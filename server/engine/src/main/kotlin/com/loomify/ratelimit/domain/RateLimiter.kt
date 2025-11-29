package com.loomify.ratelimit.domain

import reactor.core.publisher.Mono

/**
 * Output port for the rate limiting service.
 * This defines the contract for any rate limiting implementation (adapter).
 */
interface RateLimiter {
    /**
     * Consumes a token for a given identifier in a non-blocking manner.
     *
     * @param identifier The identifier to rate limit (e.g., API key or IP address).
     * @return A [Mono] of [RateLimitResult] indicating if the request was allowed or denied.
     */
    fun consumeToken(identifier: String): Mono<RateLimitResult>

    /**
     * Consumes a token for a given identifier in a non-blocking manner.
     *
     * @param identifier The identifier to rate limit (e.g., API key or IP address).
     * @param strategy The rate limiting strategy to apply.
     * @return A [Mono] of [RateLimitResult] indicating if the request was allowed or denied.
     */
    fun consumeToken(identifier: String, strategy: RateLimitStrategy): Mono<RateLimitResult>
}
