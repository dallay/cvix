package com.loomify.engine.authentication.infrastructure.ratelimit

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Duration
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
 *
 * Applies different rate limits based on the endpoint:
 * - Login: 5 attempts per 15 minutes per IP address
 * - Registration: 3 attempts per hour per IP address
 * - Password reset: 3 attempts per hour per IP address
 * - Token refresh: 10 attempts per minute per IP address
 *
 * **Requirements**: FR-010, FR-004
 *
 * @property rateLimiter The rate limiter implementation
 * @property objectMapper Jackson object mapper for JSON responses
 * @since 1.0.0
 */
@Component
class RateLimitingFilter(
    private val rateLimiter: InMemoryRateLimiter,
    private val objectMapper: ObjectMapper
) : WebFilter {

    private val logger = LoggerFactory.getLogger(RateLimitingFilter::class.java)

    companion object {
        // Rate limit configurations
        private val LOGIN_RATE_LIMIT = RateLimitConfig(
            maxAttempts = 5,
            windowDuration = Duration.ofMinutes(15),
            paths = listOf("/api/auth/login", "/api/auth/login"),
        )

        private val REGISTRATION_RATE_LIMIT = RateLimitConfig(
            maxAttempts = 3,
            windowDuration = Duration.ofHours(1),
            paths = listOf("/api/auth/register", "/api/auth/register"),
        )

        private val PASSWORD_RESET_RATE_LIMIT = RateLimitConfig(
            maxAttempts = 3,
            windowDuration = Duration.ofHours(1),
            paths = listOf("/api/password/reset", "/api/auth/password/reset"),
        )

        private val TOKEN_REFRESH_RATE_LIMIT = RateLimitConfig(
            maxAttempts = 10,
            windowDuration = Duration.ofMinutes(1),
            paths = listOf("/api/auth/refresh-token", "/api/auth/token/refresh"),
        )

        private val FEDERATED_LOGIN_RATE_LIMIT = RateLimitConfig(
            maxAttempts = 10,
            windowDuration = Duration.ofMinutes(5),
            paths = listOf("/api/auth/federated/initiate"),
        )
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.path.pathWithinApplication().value()

        // Find matching rate limit configuration
        val rateLimitConfig = findRateLimitConfig(path) ?: return chain.filter(exchange)

        // Get client identifier (IP address)
        val clientIdentifier = getClientIdentifier(exchange)

        // Check rate limit
        if (!rateLimiter.isAllowed(
                clientIdentifier,
                rateLimitConfig.maxAttempts,
                rateLimitConfig.windowDuration,
            )
        ) {
            logger.warn("Rate limit exceeded for $path from $clientIdentifier")
            return sendRateLimitResponse(exchange, clientIdentifier, rateLimitConfig)
        }

        // Add rate limit headers to response before processing
        addRateLimitHeaders(exchange, clientIdentifier, rateLimitConfig)

        return chain.filter(exchange)
    }

    private fun findRateLimitConfig(path: String): RateLimitConfig? {
        return listOf(
            LOGIN_RATE_LIMIT,
            REGISTRATION_RATE_LIMIT,
            PASSWORD_RESET_RATE_LIMIT,
            TOKEN_REFRESH_RATE_LIMIT,
            FEDERATED_LOGIN_RATE_LIMIT,
        ).firstOrNull { config ->
            config.paths.any { path.contains(it) }
        }
    }

    private fun getClientIdentifier(exchange: ServerWebExchange): String {
        // Try to get real IP from X-Forwarded-For header
        val forwardedFor = exchange.request.headers.getFirst("X-Forwarded-For")
        if (forwardedFor != null) {
            return forwardedFor.split(",").first().trim()
        }

        // Fall back to remote address
        return exchange.request.remoteAddress?.address?.hostAddress ?: "unknown"
    }

    private fun sendRateLimitResponse(
        exchange: ServerWebExchange,
        identifier: String,
        config: RateLimitConfig
    ): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.TOO_MANY_REQUESTS
        response.headers.contentType = MediaType.APPLICATION_JSON

        // Add rate limit headers
        addRateLimitHeaders(exchange, identifier, config)

        // Create error response body
        val errorResponse = mapOf(
            "error" to mapOf(
                "code" to "RATE_LIMIT_EXCEEDED",
                "message" to "Too many requests. Please try again later.",
                "timestamp" to Instant.now().toString(),
                "retryAfter" to rateLimiter.getTimeUntilReset(identifier, config.maxAttempts, config.windowDuration)?.seconds,
            ),
        )

        val bytes = objectMapper.writeValueAsBytes(errorResponse)
        val buffer: DataBuffer = response.bufferFactory().wrap(bytes)

        return response.writeWith(Mono.just(buffer))
    }

    private fun addRateLimitHeaders(
        exchange: ServerWebExchange,
        identifier: String,
        config: RateLimitConfig
    ) {
        val remaining = rateLimiter.getRemainingAttempts(
            identifier,
            config.maxAttempts,
            config.windowDuration,
        )

        val resetTime = rateLimiter.getTimeUntilReset(identifier, config.maxAttempts, config.windowDuration)

        try {
            exchange.response.headers.apply {
                set("X-RateLimit-Limit", config.maxAttempts.toString())
                set("X-RateLimit-Remaining", remaining.toString())
                resetTime?.let {
                    set("X-RateLimit-Reset", Instant.now().plus(it).epochSecond.toString())
                }
            }
        } catch (_: UnsupportedOperationException) {
            // Headers are already committed (read-only), log and continue
            logger.debug("Cannot add rate limit headers - response already committed for $identifier")
        }
    }

    /**
     * Configuration for rate limiting a specific endpoint or group of endpoints.
     */
    private data class RateLimitConfig(
        val maxAttempts: Int,
        val windowDuration: Duration,
        val paths: List<String>
    )
}
