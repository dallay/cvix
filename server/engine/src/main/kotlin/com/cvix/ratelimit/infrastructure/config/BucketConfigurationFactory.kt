package com.cvix.ratelimit.infrastructure.config

import com.cvix.ratelimit.domain.RateLimitStrategy
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import org.slf4j.LoggerFactory

/**
 * Factory responsible for creating Bucket4j bucket configurations based on rate limit strategies.
 * This class centralizes all bucket configuration logic, eliminating duplication and providing
 * a single source of truth for rate limit configuration.
 *
 * @property properties The rate limit configuration properties.
 * @since 2.0.0
 */
class BucketConfigurationFactory(
    private val properties: RateLimitProperties
) {
    private val logger = LoggerFactory.getLogger(BucketConfigurationFactory::class.java)

    /**
     * Creates a bucket configuration for the specified rate limiting strategy.
     *
     * @param strategy The rate limiting strategy to apply.
     * @param planName Optional plan name for BUSINESS strategy (defaults to "free" if not provided).
     * @return A [BucketConfiguration] configured for the specified strategy.
     * @throws IllegalArgumentException if the strategy is BUSINESS and the plan name is invalid.
     */
    fun createConfiguration(
        strategy: RateLimitStrategy,
        planName: String? = null
    ): BucketConfiguration {
        return when (strategy) {
            RateLimitStrategy.AUTH -> createAuthConfiguration()
            RateLimitStrategy.BUSINESS -> createBusinessConfiguration(planName ?: "free")
            RateLimitStrategy.RESUME -> createResumeConfiguration()
            RateLimitStrategy.WAITLIST -> createWaitlistConfiguration()
        }
    }

    /**
     * Creates a bucket configuration for authentication endpoints.
     * Applies multiple limits (e.g., per-minute and per-hour) to protect against different attack patterns.
     */
    private fun createAuthConfiguration(): BucketConfiguration {
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
     * @throws IllegalArgumentException if the plan name is not found.
     */
    private fun createBusinessConfiguration(planName: String): BucketConfiguration {
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
     */
    private fun createResumeConfiguration(): BucketConfiguration {
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
     * Creates a bucket configuration for waitlist endpoints.
     * Applies a fixed rate limit of 10 requests per minute per IP.
     */
    private fun createWaitlistConfiguration(): BucketConfiguration {
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
     * Creates a Bandwidth from a BandwidthLimit configuration using Bucket4j v8 API.
     * This method uses the simple bandwidth API which is equivalent to greedy refill
     * when refillTokens equals capacity.
     *
     * @param limit The bandwidth limit configuration.
     * @return A [Bandwidth] instance configured with the specified parameters.
     */
    private fun createBandwidth(limit: RateLimitProperties.BandwidthLimit): Bandwidth {
        return Bandwidth.simple(
            limit.capacity,
            limit.refillDuration,
        )
    }

    // ============================================
    // Configuration State Checks
    // ============================================

    /**
     * Checks if rate limiting is enabled for the specified strategy.
     */
    fun isRateLimitEnabled(strategy: RateLimitStrategy): Boolean {
        return properties.enabled && when (strategy) {
            RateLimitStrategy.AUTH -> properties.auth.enabled
            RateLimitStrategy.BUSINESS -> properties.business.enabled
            RateLimitStrategy.RESUME -> properties.resume.enabled
            RateLimitStrategy.WAITLIST -> properties.waitlist.enabled
        }
    }

    /**
     * Gets the list of endpoints that should be rate limited for the specified strategy.
     */
    fun getEndpoints(strategy: RateLimitStrategy): List<String> {
        return when (strategy) {
            RateLimitStrategy.AUTH -> properties.auth.endpoints
            RateLimitStrategy.RESUME -> properties.resume.endpoints
            RateLimitStrategy.WAITLIST -> properties.waitlist.endpoints
            RateLimitStrategy.BUSINESS -> emptyList() // Business endpoints are handled differently
        }
    }
}
