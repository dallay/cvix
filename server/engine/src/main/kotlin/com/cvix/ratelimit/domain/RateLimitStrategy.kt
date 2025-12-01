package com.cvix.ratelimit.domain

/**
 * Type of rate limiting strategy.
 */
enum class RateLimitStrategy {
    /**
     * Authentication strategy: uses strict per-minute/per-hour limits to prevent brute force attacks.
     */
    AUTH,

    /**
     * Business strategy: uses pricing plan-based limits for API usage quotas.
     */
    BUSINESS,

    /**
     * Resume strategy: uses fixed rate limit for resume generation endpoints (10 req/min per user).
     */
    RESUME
}
