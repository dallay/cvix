package com.loomify.engine.ratelimit.infrastructure.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.Refill
import org.slf4j.LoggerFactory

/**
 * Strategy for building Bucket4j bucket configurations based on rate limit properties.
 */
class BucketConfigurationStrategy(
    private val properties: RateLimitProperties
) {
    private val logger = LoggerFactory.getLogger(BucketConfigurationStrategy::class.java)

    /**
     * Creates a bucket configuration for authentication endpoints.
     * Applies multiple limits (e.g., per-minute and per-hour) to protect against different attack patterns.
     *
     * @return A [BucketConfiguration] with all configured auth limits.
     */
    fun createAuthBucketConfiguration(): BucketConfiguration {
        logger.debug("Creating auth bucket configuration with {} limits", properties.auth.limits.size)

        val builder = BucketConfiguration.builder()

        properties.auth.limits.forEach { limit ->
            val bandwidth = createBandwidth(limit)
            builder.addLimit(bandwidth)
            logger.debug("Added auth limit: {} - capacity={}, refill={} tokens per {}",
                limit.name, limit.capacity, limit.refillTokens, limit.refillDuration)
        }

        return builder.build()
    }

    /**
     * Creates a bucket configuration for business endpoints based on a pricing plan.
     *
     * @param planName The name of the pricing plan (e.g., "free", "basic", "professional").
     * @return A [BucketConfiguration] with the limit for the specified plan.
     * @throws IllegalArgumentException if the plan name is not found.
     */
    fun createBusinessBucketConfiguration(planName: String): BucketConfiguration {
        val limit = properties.business.pricingPlans[planName.lowercase()]
            ?: throw IllegalArgumentException("Unknown pricing plan: $planName. Available plans: ${properties.business.pricingPlans.keys}")

        logger.debug("Creating business bucket configuration for plan: {} - capacity={}, refill={} tokens per {}",
            planName, limit.capacity, limit.refillTokens, limit.refillDuration)

        val bandwidth = createBandwidth(limit)
        return BucketConfiguration.builder()
            .addLimit(bandwidth)
            .build()
    }

    /**
     * Creates a Bandwidth from a BandwidthLimit configuration using Bucket4j v8 API.
     *
     * @param limit The bandwidth limit configuration.
     * @return A [Bandwidth] instance configured with the specified parameters.
     */
    private fun createBandwidth(limit: RateLimitProperties.BandwidthLimit): Bandwidth {
        // Bucket4j v8 uses Bandwidth.classic() with Refill strategies
        return Bandwidth.classic(
            limit.capacity,
            Refill.greedy(limit.refillTokens, limit.refillDuration)
        )
    }

    /**
     * Gets the list of authentication endpoints that should be rate limited.
     */
    fun getAuthEndpoints(): List<String> = properties.auth.endpoints

    /**
     * Checks if authentication rate limiting is enabled.
     */
    fun isAuthRateLimitEnabled(): Boolean = properties.enabled && properties.auth.enabled

    /**
     * Checks if business rate limiting is enabled.
     */
    fun isBusinessRateLimitEnabled(): Boolean = properties.enabled && properties.business.enabled
}

