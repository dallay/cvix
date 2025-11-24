package com.loomify.engine.ratelimit.infrastructure.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
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
            logger.debug(
                "Added auth limit: {} - capacity={}, refill={} tokens per {}",
                limit.name, limit.capacity, limit.refillTokens, limit.refillDuration,
            )
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
            ?: throw IllegalArgumentException(
                "Unknown pricing plan: $planName. Available plans: ${properties.business.pricingPlans.keys}",
            )

        logger.debug(
            "Creating business bucket configuration for plan: {} - capacity={}, refill={} tokens per {}",
            planName, limit.capacity, limit.refillTokens, limit.refillDuration,
        )

        val bandwidth = createBandwidth(limit)
        return BucketConfiguration.builder()
            .addLimit(bandwidth)
            .build()
    }

    /**
     * Creates a bucket configuration for resume generation endpoints.
     * Applies a fixed rate limit of 10 requests per minute per user.
     *
     * @return A [BucketConfiguration] with the resume endpoint limit.
     */
    fun createResumeBucketConfiguration(): BucketConfiguration {
        val limit = properties.resume.limit

        logger.debug(
            "Creating resume bucket configuration - capacity={}, refill={} tokens per {}",
            limit.capacity, limit.refillTokens, limit.refillDuration,
        )

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
        // Updated to use Bucket4j v8 builder API (no deprecated methods)
        return Bandwidth.builder()
            .capacity(limit.capacity)
            .refillGreedy(limit.refillTokens, limit.refillDuration)
            .initialTokens(limit.capacity)
            .build()
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
     * Gets the list of resume endpoints that should be rate limited.
     */
    fun getResumeEndpoints(): List<String> = properties.resume.endpoints

    /**
     * Checks if resume rate limiting is enabled.
     */
    fun isResumeRateLimitEnabled(): Boolean = properties.enabled && properties.resume.enabled

    /**
     * Checks if business rate limiting is enabled.
     */
    fun isBusinessRateLimitEnabled(): Boolean = properties.enabled && properties.business.enabled
}
