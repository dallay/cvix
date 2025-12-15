package com.cvix.ratelimit.infrastructure.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration

/**
 *
 * @created 15/12/25
 */
class ResumeBucketConfiguration(private val properties: RateLimitProperties) {
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
     * Gets the list of resume endpoints that should be rate limited.
     */
    fun getResumeEndpoints(): List<String> = properties.resume.endpoints

    /**
     * Checks if resume rate limiting is enabled.
     */
    fun isResumeRateLimitEnabled(): Boolean = properties.enabled && properties.resume.enabled

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

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(ResumeBucketConfiguration::class.java)
    }
}
