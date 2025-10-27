package com.loomify.engine.ratelimit.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.loomify.engine.ratelimit.application.RateLimitingService
import com.loomify.engine.ratelimit.domain.RateLimitResult
import com.loomify.engine.ratelimit.infrastructure.config.BucketConfigurationStrategy
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
 * This filter uses the application's [com.loomify.engine.ratelimit.application.RateLimitingService]
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

        if (shouldSkipRateLimiting(exchange, path)) {
            return chain.filter(exchange)
        }

        val identifier = getIdentifier(exchange)
        logger.debug("Resolved identifier: {} for path: {}", identifier, path)

        return rateLimitingService.consumeToken(identifier, path, RateLimitStrategy.AUTH)
            .flatMap { result ->
                when (result) {
                    is RateLimitResult.Allowed -> {
                        logger.debug("Request allowed for identifier {}", identifier)
                        addRateLimitHeaders(exchange, result)
                        chain.filter(exchange)
                    }
                    is RateLimitResult.Denied -> {
                        logger.warn("Rate limit exceeded for identifier {} on path {}", identifier, path)
                        sendRateLimitResponse(exchange, result, path)
                    }
                }
            }
    }

    private fun shouldSkipRateLimiting(exchange: ServerWebExchange, path: String): Boolean {
        val alreadyProcessed = exchange.attributes.putIfAbsent(RATE_LIMIT_PROCESSED_KEY, true) != null
        if (alreadyProcessed) {
            logger.debug("Request already processed by rate limiter, skipping")
            return true
        }

        val isAuthEndpoint = isAuthenticationEndpoint(path)
        val isRateLimitEnabled = configurationStrategy.isAuthRateLimitEnabled()

        return when {
            !isAuthEndpoint -> {
                logger.debug("Path {} is not an authentication endpoint, skipping rate limit", path)
                true
            }
            !isRateLimitEnabled -> {
                logger.debug("Authentication rate limiting is disabled, skipping")
                true
            }
            else -> false
        }
    }

    private fun isAuthenticationEndpoint(path: String): Boolean {
        val authEndpoints = configurationStrategy.getAuthEndpoints()
        return authEndpoints.any { path.contains(it) }
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
        path: String
    ): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.TOO_MANY_REQUESTS
        response.headers.contentType = MediaType.APPLICATION_JSON

        val retryAfterSeconds = result.retryAfter.seconds
        response.headers.set("X-Rate-Limit-Retry-After-Seconds", retryAfterSeconds.toString())

        val errorResponse = mapOf(
            "error" to mapOf(
                "code" to "RATE_LIMIT_EXCEEDED",
                "message" to "Too many authentication attempts. Please try again later.",
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
