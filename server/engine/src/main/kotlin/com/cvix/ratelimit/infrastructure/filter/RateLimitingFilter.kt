package com.cvix.ratelimit.infrastructure.filter

import com.cvix.ratelimit.application.RateLimitingService
import com.cvix.ratelimit.domain.RateLimitResult
import com.cvix.ratelimit.domain.RateLimitStrategy
import com.cvix.ratelimit.infrastructure.config.BucketConfigurationFactory
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Instant
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * WebFlux filter for rate limiting endpoints based on configured strategies.
 * This filter uses the application's [RateLimitingService] to apply rate limits
 * based on IP addresses or other identifiers, following the hexagonal architecture.
 *
 * Supported strategies:
 * - AUTH: Strict time-based limits (per-minute, per-hour) to prevent brute force attacks
 * - RESUME: Fixed rate limits for resume generation endpoints
 * - WAITLIST: Fixed rate limits for waitlist endpoints to prevent spam
 * - BUSINESS: Pricing plan-based limits for API usage quotas (not enforced in filter)
 *
 * @property rateLimitingService The application service for rate limiting.
 * @property objectMapper Jackson object mapper for JSON responses.
 * @property configurationFactory Factory for determining rate limit configuration.
 * @since 2.0.0
 */
@Component
class RateLimitingFilter(
    private val rateLimitingService: RateLimitingService,
    private val objectMapper: ObjectMapper,
    private val configurationFactory: BucketConfigurationFactory
) : WebFilter {

    private val logger = LoggerFactory.getLogger(RateLimitingFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.path.pathWithinApplication().value()
        logger.debug("Rate limit filter invoked for path: {}", path)

        val strategy = determineRateLimitStrategy(path)
        if (strategy == null || shouldSkipRateLimiting(exchange, strategy)) {
            return chain.filter(exchange)
        }

        val identifier = getIdentifier(exchange)
        logger.debug("Resolved identifier: {} for path: {} with strategy: {}", identifier, path, strategy)

        return rateLimitingService.consumeToken(identifier, path, strategy)
            .flatMap { result ->
                when (result) {
                    is RateLimitResult.Allowed -> {
                        logger.debug("Request allowed for identifier {}", identifier)
                        addRateLimitHeaders(exchange, result)
                        chain.filter(exchange)
                    }
                    is RateLimitResult.Denied -> {
                        logger.warn("Rate limit exceeded for identifier {} on path {}", identifier, path)
                        sendRateLimitResponse(exchange, result, path, strategy)
                    }
                }
            }
    }

    /**
     * Determines the rate limiting strategy based on the request path.
     */
    private fun determineRateLimitStrategy(path: String): RateLimitStrategy? {
        return when {
            isStrategyEndpoint(path, RateLimitStrategy.AUTH) -> RateLimitStrategy.AUTH
            isStrategyEndpoint(path, RateLimitStrategy.RESUME) -> RateLimitStrategy.RESUME
            isStrategyEndpoint(path, RateLimitStrategy.WAITLIST) -> RateLimitStrategy.WAITLIST
            else -> null
        }
    }

    /**
     * Checks if the path matches any endpoint for the given strategy.
     */
    private fun isStrategyEndpoint(path: String, strategy: RateLimitStrategy): Boolean {
        val endpoints = configurationFactory.getEndpoints(strategy)
        return endpoints.any { path.contains(it) }
    }

    /**
     * Determines if rate limiting should be skipped for this request.
     */
    private fun shouldSkipRateLimiting(
        exchange: ServerWebExchange,
        strategy: RateLimitStrategy
    ): Boolean {
        val alreadyProcessed = exchange.attributes.putIfAbsent(RATE_LIMIT_PROCESSED_KEY, true) != null
        if (alreadyProcessed) {
            logger.debug("Request already processed by rate limiter, skipping")
            return true
        }

        val isRateLimitEnabled = configurationFactory.isRateLimitEnabled(strategy)
        if (!isRateLimitEnabled) {
            logger.debug("Rate limiting is disabled for strategy {}, skipping", strategy)
            return true
        }

        return false
    }

    /**
     * Extracts the identifier from the request.
     * For most strategies, we use IP address to prevent abuse.
     */
    private fun getIdentifier(exchange: ServerWebExchange): String {
        // For authentication/waitlist endpoints, we primarily use IP address to prevent brute force attacks
        // API keys are not typically present in these requests
        val forwardedFor = exchange.request.headers.getFirst("X-Forwarded-For")
        if (forwardedFor != null) {
            return "IP:${forwardedFor.split(",").first().trim()}"
        }
        return "IP:${exchange.request.remoteAddress?.address?.hostAddress ?: "unknown"}"
    }

    /**
     * Adds standard rate limit headers to the response.
     * Following RFC 6585 and industry best practices (GitHub, Twitter, Stripe).
     *
     * Headers:
     * - X-RateLimit-Limit: Maximum number of requests allowed in the time window
     * - X-RateLimit-Remaining: Number of requests remaining in the current time window
     * - X-RateLimit-Reset: Unix timestamp (seconds) when the rate limit resets
     */
    private fun addRateLimitHeaders(exchange: ServerWebExchange, result: RateLimitResult.Allowed) {
        val response = exchange.response
        response.headers.set("X-RateLimit-Limit", result.limitCapacity.toString())
        response.headers.set("X-RateLimit-Remaining", result.remainingTokens.toString())
        response.headers.set("X-RateLimit-Reset", result.resetTime.epochSecond.toString())
        logger.debug(
            "Added rate limit headers: limit={}, remaining={}, reset={}",
            result.limitCapacity, result.remainingTokens, result.resetTime,
        )
    }

    /**
     * Sends a 429 Too Many Requests response with detailed error information.
     * Includes standard HTTP rate limit headers per RFC 6585.
     *
     * Headers:
     * - X-RateLimit-Limit: Maximum number of requests allowed
     * - Retry-After: Standard HTTP header (seconds) when to retry
     * - X-Rate-Limit-Retry-After-Seconds: Custom header (deprecated, kept for backward compatibility)
     */
    private fun sendRateLimitResponse(
        exchange: ServerWebExchange,
        result: RateLimitResult.Denied,
        path: String,
        strategy: RateLimitStrategy
    ): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.TOO_MANY_REQUESTS
        response.headers.contentType = MediaType.APPLICATION_JSON

        val retryAfterSeconds = result.retryAfter.seconds

        // Standard headers
        response.headers.set("X-RateLimit-Limit", result.limitCapacity.toString())
        response.headers.set("Retry-After", retryAfterSeconds.toString()) // Standard HTTP header

        // Custom header (kept for backward compatibility)
        response.headers.set("X-Rate-Limit-Retry-After-Seconds", retryAfterSeconds.toString())

        val message = when (strategy) {
            RateLimitStrategy.AUTH -> "Too many authentication attempts. Please try again later."
            RateLimitStrategy.RESUME -> "Rate limit exceeded for resume generation. Please try again later."
            RateLimitStrategy.WAITLIST -> "Too many waitlist requests. Please try again later."
            else -> "Rate limit exceeded. Please try again later."
        }

        val errorResponse = mapOf(
            "error" to mapOf(
                "code" to "RATE_LIMIT_EXCEEDED",
                "message" to message,
                "timestamp" to Instant.now().toString(),
                "retryAfter" to retryAfterSeconds,
                "path" to path,
            ),
        )

        val bytes = objectMapper.writeValueAsBytes(errorResponse)
        val buffer: DataBuffer = response.bufferFactory().wrap(bytes)
        return response.writeWith(Mono.just(buffer))
    }

    companion object {
        private const val RATE_LIMIT_PROCESSED_KEY = "rateLimitProcessed"
    }
}
