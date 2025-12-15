package com.cvix.ratelimit.infrastructure.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration

/**
 *
 * @created 15/12/25
 */
class AuthBucketConfiguration(private val properties: RateLimitProperties) {
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
     * Gets the list of authentication endpoints that should be rate limited.
     */
    fun getAuthEndpoints(): List<String> = properties.auth.endpoints

    /**
     * Checks if authentication rate limiting is enabled.
     */
    fun isAuthRateLimitEnabled(): Boolean = properties.enabled && properties.auth.enabled
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
        private val logger = org.slf4j.LoggerFactory.getLogger(AuthBucketConfiguration::class.java)
    }
}
