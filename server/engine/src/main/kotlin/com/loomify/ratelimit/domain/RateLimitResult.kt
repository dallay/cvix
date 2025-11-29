package com.loomify.ratelimit.domain

import java.time.Duration

/**
 * Represents the result of a rate limit consumption attempt.
 * This is a framework-agnostic model, part of the application layer.
 */
sealed class RateLimitResult {
    /**
     * The request was allowed.
     *
     * @property remainingTokens The number of tokens left.
     */
    data class Allowed(val remainingTokens: Long) : RateLimitResult()

    /**
     * The request was denied.
     *
     * @property retryAfter The duration until the next token is available.
     */
    data class Denied(val retryAfter: Duration) : RateLimitResult()
}
