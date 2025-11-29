package com.cvix.ratelimit.infrastructure

import com.cvix.ratelimit.application.RateLimitingService
import com.cvix.ratelimit.domain.RateLimitResult
import com.cvix.ratelimit.domain.RateLimitStrategy
import com.cvix.ratelimit.infrastructure.config.BucketConfigurationStrategy
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
 * WebFlux filter for rate limiting authentication endpoints.
 * This filter uses the application's [RateLimitingService]
 * to apply rate limits based on IP addresses for authentication endpoints, following the hexagonal architecture.
 *
 * Authentication endpoints use strict time-based limits (per-minute, per-hour) to prevent brute force attacks,
 * while business endpoints use pricing plan-based limits for API usage quotas.
 *
 * @property rateLimitingService The application service for rate limiting.
 * @property objectMapper Jackson object mapper for JSON responses.
 * @property configurationStrategy Strategy for determining rate limit configuration.
 */
@Component
class RateLimitingFilter(
    private val rateLimitingService: RateLimitingService,
    private val objectMapper: ObjectMapper,
    private val configurationStrategy: BucketConfigurationStrategy
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

    private fun determineRateLimitStrategy(path: String): RateLimitStrategy? {
        return when {
            isAuthenticationEndpoint(path) -> RateLimitStrategy.AUTH
            isResumeEndpoint(path) -> RateLimitStrategy.RESUME
            else -> null
        }
    }

    private fun shouldSkipRateLimiting(
        exchange: ServerWebExchange,
        strategy: RateLimitStrategy
    ): Boolean {
        val alreadyProcessed = exchange.attributes.putIfAbsent(RATE_LIMIT_PROCESSED_KEY, true) != null
        if (alreadyProcessed) {
            logger.debug("Request already processed by rate limiter, skipping")
            return true
        }

        val isRateLimitEnabled = when (strategy) {
            RateLimitStrategy.AUTH -> configurationStrategy.isAuthRateLimitEnabled()
            RateLimitStrategy.RESUME -> configurationStrategy.isResumeRateLimitEnabled()
            else -> false
        }

        if (!isRateLimitEnabled) {
            logger.debug("Rate limiting is disabled for strategy {}, skipping", strategy)
            return true
        }
        return false
    }

    private fun isAuthenticationEndpoint(path: String): Boolean {
        val authEndpoints = configurationStrategy.getAuthEndpoints()
        return authEndpoints.any { path.contains(it) }
    }

    private fun isResumeEndpoint(path: String): Boolean {
        val resumeEndpoints = configurationStrategy.getResumeEndpoints()
        return resumeEndpoints.any { path.contains(it) }
    }

    private fun getIdentifier(exchange: ServerWebExchange): String {
        // For authentication endpoints, we primarily use IP address to prevent brute force attacks
        // API keys are not typically present in auth requests
        val forwardedFor = exchange.request.headers.getFirst("X-Forwarded-For")
        if (forwardedFor != null) {
            return "IP:${forwardedFor.split(",").first().trim()}"
        }
        return "IP:${exchange.request.remoteAddress?.address?.hostAddress ?: "unknown"}"
    }

    private fun addRateLimitHeaders(exchange: ServerWebExchange, result: RateLimitResult.Allowed) {
        val response = exchange.response
        response.headers.set("X-Rate-Limit-Remaining", result.remainingTokens.toString())
        logger.debug("Added rate limit headers: remaining={}", result.remainingTokens)
    }

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
        response.headers.set("X-Rate-Limit-Retry-After-Seconds", retryAfterSeconds.toString())

        val message = when (strategy) {
            RateLimitStrategy.AUTH -> "Too many authentication attempts. Please try again later."
            RateLimitStrategy.RESUME -> "Rate limit exceeded for resume generation. Please try again later."
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
