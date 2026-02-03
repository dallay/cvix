package com.cvix.ratelimit.application

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.ratelimit.domain.RateLimitResult
import com.cvix.ratelimit.domain.RateLimitStrategy
import com.cvix.ratelimit.domain.RateLimiter
import com.cvix.ratelimit.domain.event.RateLimitExceededEvent
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

/**
 * Application service for handling rate limiting logic.
 * This service acts as a use case handler, orchestrating the interaction
 * between the incoming request (port in) and the rate limiting implementation (port out).
 * All operations are non-blocking using reactive types.
 */
@Service
class RateLimitingService(
    private val rateLimiter: RateLimiter,
    private val eventPublisher: EventPublisher<RateLimitExceededEvent>
) {

    /**
     * Consumes a token for a given identifier using the default BUSINESS strategy.
     *
     * @param identifier The identifier to rate limit (e.g., API key or IP address).
     * @param endpoint The endpoint being accessed.
     * @return A [Mono] of [RateLimitResult] indicating if the request was allowed or denied.
     */
    fun consumeToken(identifier: String, endpoint: String): Mono<RateLimitResult> =
        consumeToken(identifier, endpoint, RateLimitStrategy.BUSINESS)

    /**
     * Consumes a token for a given identifier using a specific strategy and publishes an event
     * if the rate limit is exceeded.
     *
     * @param identifier The identifier to rate limit (e.g., API key or IP address).
     * @param endpoint The endpoint being accessed.
     * @param strategy The rate limiting strategy to apply.
     * @return A [Mono] of [RateLimitResult] indicating if the request was allowed or denied.
     */
    fun consumeToken(
        identifier: String,
        endpoint: String,
        strategy: RateLimitStrategy
    ): Mono<RateLimitResult> {
        return rateLimiter.consumeToken(identifier, strategy)
            .flatMap { result ->
                if (result is RateLimitResult.Denied) {
                    publishRateLimitExceededEvent(identifier, endpoint, result.retryAfter, strategy)
                        .thenReturn(result)
                } else {
                    Mono.just(result)
                }
            }
    }

    private fun publishRateLimitExceededEvent(
        identifier: String,
        endpoint: String,
        retryAfter: Duration,
        strategy: RateLimitStrategy
    ): Mono<Void> = mono {
        val event = RateLimitExceededEvent(
            identifier = identifier,
            endpoint = endpoint,
            attemptCount = null, // Bucket4j doesn't track individual attempts
            maxAttempts = null, // Bucket4j doesn't track individual attempts
            windowDuration = retryAfter,
            strategy = strategy,
            resetTime = Instant.now().plus(retryAfter),
        )
        eventPublisher.publish(event)
    }.then()
}
