package com.cvix.ratelimit.domain

import java.time.Duration
import java.time.Instant

/**
 * Represents the result of a rate limit consumption attempt.
 * This is a framework-agnostic model, part of the application layer.
 */
sealed class RateLimitResult {
    /**
     * The request was allowed.
     *
     * @property remainingTokens The number of tokens left in the bucket.
     * @property limitCapacity The maximum number of tokens the bucket can hold (for X-RateLimit-Limit header).
     * @property resetTime The timestamp when the bucket will be fully refilled (for X-RateLimit-Reset header).
     */
    data class Allowed(
        val remainingTokens: Long,
        val limitCapacity: Long,
        val resetTime: Instant
    ) : RateLimitResult()

    /**
     * The request was denied.
     *
     * @property retryAfter The duration until the next token is available.
     * @property limitCapacity The maximum number of tokens the bucket can hold (for X-RateLimit-Limit header).
     */
    data class Denied(
        val retryAfter: Duration,
        val limitCapacity: Long
    ) : RateLimitResult()
}
