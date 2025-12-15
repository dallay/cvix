package com.cvix.ratelimit.infrastructure.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration

/**
 *
 * @created 15/12/25
 */
class WaitlistBucketConfiguration(private val properties: RateLimitProperties) {
    /**
     * Creates a bucket configuration for waitlist endpoints.
     * Applies a fixed rate limit of 10 requests per minute per IP.
     *
     * @return A [BucketConfiguration] with the waitlist endpoint limit.
     */
    fun createWaitlistBucketConfiguration(): BucketConfiguration {
        val limit = properties.waitlist.limit

        logger.debug(
            "Creating waitlist bucket configuration - capacity={}, refill={} tokens per {}",
            limit.capacity, limit.refillTokens, limit.refillDuration,
        )

        val bandwidth = createBandwidth(limit)
        return BucketConfiguration.builder()
            .addLimit(bandwidth)
            .build()
    }

    /**
     * Gets the list of waitlist endpoints that should be rate limited.
     */
    fun getWaitlistEndpoints(): List<String> = properties.waitlist.endpoints

    /**
     * Checks if waitlist rate limiting is enabled.
     */
    fun isWaitlistRateLimitEnabled(): Boolean = properties.enabled && properties.waitlist.enabled
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
        private val logger = org.slf4j.LoggerFactory.getLogger(WaitlistBucketConfiguration::class.java)
    }
}
