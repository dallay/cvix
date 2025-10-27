package com.loomify.engine.ratelimit.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Configuration properties for rate limiting.
 * These properties can be configured in application.yml under the prefix "application.rate-limit".
 *
 * Example configuration:
 * ```yaml
 * application:
 *   rate-limit:
 *     enabled: true
 *     auth:
 *       enabled: true
 *       limits:
 *         - name: per-minute
 *           capacity: 10
 *           refill-tokens: 10
 *           refill-duration: 1m
 *         - name: per-hour
 *           capacity: 100
 *           refill-tokens: 100
 *           refill-duration: 1h
 *     business:
 *       enabled: true
 *       pricing-plans:
 *         free:
 *           capacity: 20
 *           refill-tokens: 20
 *           refill-duration: 1h
 *         basic:
 *           capacity: 40
 *           refill-tokens: 40
 *           refill-duration: 1h
 *         professional:
 *           capacity: 100
 *           refill-tokens: 100
 *           refill-duration: 1h
 * ```
 */
@ConfigurationProperties(prefix = "application.rate-limit")
data class RateLimitProperties(
    /**
     * Whether rate limiting is enabled globally.
     */
    val enabled: Boolean = true,

    /**
     * Configuration for authentication endpoints rate limiting.
     */
    val auth: AuthRateLimitConfig = AuthRateLimitConfig(),

    /**
     * Configuration for business endpoints rate limiting.
     */
    val business: BusinessRateLimitConfig = BusinessRateLimitConfig()
) {

    /**
     * Configuration for authentication endpoint rate limiting.
     * These limits are applied per IP/identifier to prevent brute force attacks.
     */
    data class AuthRateLimitConfig(
        /**
         * Whether authentication rate limiting is enabled.
         */
        val enabled: Boolean = true,

        /**
         * List of endpoints that should be rate limited as authentication endpoints.
         */
        val endpoints: List<String> = listOf(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/password/reset",
            "/api/auth/refresh-token",
            "/api/auth/token/refresh",
            "/api/auth/federated"
        ),

        /**
         * Multiple limits that can be applied simultaneously (e.g., per-minute + per-hour).
         * This allows for protection against both burst attacks and sustained attacks.
         */
        val limits: List<BandwidthLimit> = listOf(
            BandwidthLimit(
                name = "per-minute",
                capacity = 10,
                refillTokens = 10,
                refillDuration = Duration.ofMinutes(1)
            ),
            BandwidthLimit(
                name = "per-hour",
                capacity = 100,
                refillTokens = 100,
                refillDuration = Duration.ofHours(1)
            )
        )
    )

    /**
     * Configuration for business endpoint rate limiting.
     * These limits are applied based on pricing plans.
     */
    data class BusinessRateLimitConfig(
        /**
         * Whether business rate limiting is enabled.
         */
        val enabled: Boolean = true,

        /**
         * Pricing plans configuration.
         * Keys are plan names (e.g., "free", "basic", "professional").
         */
        val pricingPlans: Map<String, BandwidthLimit> = mapOf(
            "free" to BandwidthLimit(
                name = "free-plan",
                capacity = 20,
                refillTokens = 20,
                refillDuration = Duration.ofHours(1)
            ),
            "basic" to BandwidthLimit(
                name = "basic-plan",
                capacity = 40,
                refillTokens = 40,
                refillDuration = Duration.ofHours(1)
            ),
            "professional" to BandwidthLimit(
                name = "professional-plan",
                capacity = 100,
                refillTokens = 100,
                refillDuration = Duration.ofHours(1)
            )
        )
    )

    /**
     * Represents a single bandwidth limit configuration.
     */
    data class BandwidthLimit(
        /**
         * Unique name/identifier for this limit (useful for logging and debugging).
         */
        val name: String,

        /**
         * Maximum number of tokens the bucket can hold.
         */
        val capacity: Long,

        /**
         * Number of tokens to add during each refill.
         */
        val refillTokens: Long,

        /**
         * Duration between refills.
         */
        val refillDuration: Duration,

        /**
         * Initial number of tokens in the bucket. Defaults to capacity if not specified.
         */
        val initialTokens: Long? = null
    )
}

