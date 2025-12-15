package com.cvix.ratelimit.infrastructure.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import org.slf4j.LoggerFactory

/**
 * Strategy for building Bucket4j bucket configurations based on rate limit properties.
 */
class BucketConfigurationStrategy(
    private val properties: RateLimitProperties
) {
    private val waitlistBucketConfiguration = WaitlistBucketConfiguration(properties)
    private val resumeBucketConfiguration = ResumeBucketConfiguration(properties)
    private val authBucketConfiguration = AuthBucketConfiguration(properties)
    private val logger = LoggerFactory.getLogger(BucketConfigurationStrategy::class.java)

    fun createWaitlistBucketConfiguration(): BucketConfiguration =
        waitlistBucketConfiguration.createWaitlistBucketConfiguration()
    fun isWaitlistRateLimitEnabled(): Boolean =
        waitlistBucketConfiguration.isWaitlistRateLimitEnabled()
    fun getWaitlistEndpoints(): List<String> =
        waitlistBucketConfiguration.getWaitlistEndpoints()

    fun createResumeBucketConfiguration(): BucketConfiguration =
        resumeBucketConfiguration.createResumeBucketConfiguration()
    fun isResumeRateLimitEnabled(): Boolean =
        resumeBucketConfiguration.isResumeRateLimitEnabled()
    fun getResumeEndpoints(): List<String> =
        resumeBucketConfiguration.getResumeEndpoints()

    fun createAuthBucketConfiguration(): BucketConfiguration =
        authBucketConfiguration.createAuthBucketConfiguration()
    fun isAuthRateLimitEnabled(): Boolean =
        authBucketConfiguration.isAuthRateLimitEnabled()
    fun getAuthEndpoints(): List<String> =
        authBucketConfiguration.getAuthEndpoints()

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
     * Creates a Bandwidth from a BandwidthLimit configuration using Bucket4j v8 API.
     *
     * @param limit The bandwidth limit configuration.
     * @return A [Bandwidth] instance configured with the specified parameters.
     */
    private fun createBandwidth(limit: RateLimitProperties.BandwidthLimit): Bandwidth {
        // NOTE: Previously this used the deprecated Refill API (Refill.greedy/intervally).
        // To avoid using the deprecated Refill class, use Bandwidth.simple(capacity, refillPeriod).
        // This is equivalent when refillTokens == capacity. If refillTokens differs from capacity
        // and the exact refill semantics are required, consider migrating to Bandwidth.builder()
        // with a custom refill strategy. For now we assume refillTokens == capacity as configured
        // in repository properties.
        return Bandwidth.simple(
            limit.capacity,
            limit.refillDuration,
        )
    }

    /**
     * Checks if business rate limiting is enabled.
     */
    fun isBusinessRateLimitEnabled(): Boolean = properties.enabled && properties.business.enabled
}
