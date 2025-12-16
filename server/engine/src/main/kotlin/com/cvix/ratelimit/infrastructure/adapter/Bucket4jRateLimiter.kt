package com.cvix.ratelimit.infrastructure.adapter

import com.cvix.ratelimit.domain.RateLimitResult
import com.cvix.ratelimit.domain.RateLimitStrategy
import com.cvix.ratelimit.domain.RateLimiter
import com.cvix.ratelimit.infrastructure.config.BucketConfigurationFactory
import com.cvix.ratelimit.infrastructure.metrics.RateLimitMetrics
import com.cvix.subscription.domain.SubscriptionTier
import io.github.bucket4j.Bucket
import io.github.bucket4j.ConsumptionProbe
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * Adapter that implements the RateLimiter port using Bucket4j.
 * This class is responsible for the actual rate limiting logic using the Bucket4j library.
 * Operations are executed on a bounded elastic scheduler to avoid blocking the reactive pipeline.
 *
 * This implementation supports multiple strategies:
 * - AUTH: For authentication endpoints, using time-based limits (per-minute, per-hour)
 * - BUSINESS: For business endpoints, using pricing plan-based limits
 * - RESUME: For resume generation endpoints, using fixed rate limits per user
 * - WAITLIST: For waitlist endpoints, using fixed rate limits per IP
 *
 * @property configurationFactory Factory for creating bucket configurations.
 * @property metrics Metrics collector for rate limiting operations.
 * @since 2.0.0
 */
@Component
class Bucket4jRateLimiter(
    private val configurationFactory: BucketConfigurationFactory,
    private val metrics: RateLimitMetrics,
    private var clock: java.time.Clock = java.time.Clock.systemUTC()
) : RateLimiter {

    private val logger = LoggerFactory.getLogger(Bucket4jRateLimiter::class.java)
    private val cache = ConcurrentHashMap<String, Bucket>()

    override fun consumeToken(identifier: String): Mono<RateLimitResult> =
        consumeToken(identifier, RateLimitStrategy.BUSINESS)

    /**
     * Consumes a token with a specific rate limiting strategy.
     *
     * @param identifier The identifier to rate limit (e.g., API key or IP address).
     * @param strategy The rate limiting strategy to apply.
     * @return A [Mono] of [RateLimitResult] indicating if the request was allowed or denied.
     */
    override fun consumeToken(
        identifier: String,
        strategy: RateLimitStrategy
    ): Mono<RateLimitResult> {
        return Mono.fromCallable {
            // Record token consumption time and update cache size metric
            metrics.recordTokenConsumption(strategy) {
                val cacheKey = "${strategy.name}:$identifier"
                val bucket = cache.computeIfAbsent(cacheKey) { createBucket(identifier, strategy) }

                // Update cache size metric after potential cache insertion
                metrics.updateCacheSize(cache.size)

                val probe: ConsumptionProbe = bucket.tryConsumeAndReturnRemaining(1)

                // Get the bucket configuration to extract metadata
                val configuration = getBucketConfiguration(identifier, strategy)
                val limitCapacity = configuration.bandwidths.maxOf { it.capacity }
                val refillDuration = configuration.bandwidths.maxOf { it.refillPeriodNanos }

                val result = if (probe.isConsumed) {
                    val resetTime = calculateResetTime(refillDuration)
                    logger.debug(
                        "Token consumed for identifier: {}, strategy: {}, remaining: {}, limit: {}, reset: {}",
                        identifier, strategy, probe.remainingTokens, limitCapacity, resetTime,
                    )
                    RateLimitResult.Allowed(
                        remainingTokens = probe.remainingTokens,
                        limitCapacity = limitCapacity,
                        resetTime = resetTime,
                    )
                } else {
                    val retryAfter = Duration.ofNanos(probe.nanosToWaitForRefill)
                    logger.warn(
                        "Rate limit exceeded for identifier: {}, strategy: {}, retry after: {}, limit: {}",
                        identifier, strategy, retryAfter, limitCapacity,
                    )
                    RateLimitResult.Denied(
                        retryAfter = retryAfter,
                        limitCapacity = limitCapacity,
                    )
                }

                // Record metrics for this rate limit check
                metrics.recordRateLimitCheck(strategy, result)

                result
            }
        }.subscribeOn(Schedulers.boundedElastic())
    }

    /**
     * Creates a new bucket for the given identifier and strategy.
     *
     * @param identifier The identifier to rate limit.
     * @param strategy The rate limiting strategy to apply.
     * @return A configured [Bucket] instance.
     */
    private fun createBucket(identifier: String, strategy: RateLimitStrategy): Bucket {
        logger.debug("Creating {} bucket for identifier: {}", strategy, identifier)

        val configuration = getBucketConfiguration(identifier, strategy)

        // Build bucket with the configured limits
        val builder = Bucket.builder()
        configuration.bandwidths.forEach { bandwidth ->
            builder.addLimit(bandwidth)
        }
        return builder.build()
    }

    /**
     * Gets the bucket configuration for the given identifier and strategy.
     * This method is extracted to allow reuse for metadata extraction.
     *
     * @param identifier The identifier to rate limit.
     * @param strategy The rate limiting strategy to apply.
     * @return A [io.github.bucket4j.BucketConfiguration] instance.
     */
    private fun getBucketConfiguration(
        identifier: String,
        strategy: RateLimitStrategy
    ): io.github.bucket4j.BucketConfiguration {
        return when (strategy) {
            RateLimitStrategy.AUTH ->
                configurationFactory.createConfiguration(RateLimitStrategy.AUTH)

            RateLimitStrategy.BUSINESS -> {
                val planName = resolvePlanNameFromApiKey(identifier)
                logger.debug(
                    "Resolved plan: {} for identifier: {}",
                    planName, identifier,
                )
                configurationFactory.createConfiguration(RateLimitStrategy.BUSINESS, planName)
            }

            RateLimitStrategy.RESUME ->
                configurationFactory.createConfiguration(RateLimitStrategy.RESUME)

            RateLimitStrategy.WAITLIST ->
                configurationFactory.createConfiguration(RateLimitStrategy.WAITLIST)
        }
    }

    /**
     * Calculates the reset time based on the refill duration.
     * This is an approximation since Bucket4j doesn't expose the exact refill schedule.
     *
     * @param refillPeriodNanos The refill period in nanoseconds.
     * @return The [java.time.Instant] when the bucket will be refilled.
     */
    private fun calculateResetTime(refillPeriodNanos: Long): java.time.Instant {
        val refillDuration = Duration.ofNanos(refillPeriodNanos)
        return java.time.Instant.now(clock).plus(refillDuration)
    }

    /**
     * Resolves the pricing plan name from an API key.
     * This method uses the SubscriptionTier to determine the plan.
     *
     * @param apiKey The API key.
     * @return The plan name in lowercase (e.g., "free", "basic", "professional").
     */
    private fun resolvePlanNameFromApiKey(apiKey: String): String {
        val tier = when {
            apiKey.startsWith("PX001-") -> SubscriptionTier.PROFESSIONAL
            apiKey.startsWith("BX001-") -> SubscriptionTier.BASIC
            else -> SubscriptionTier.FREE
        }
        return tier.name.lowercase()
    }

    /**
     * Returns the current cache size.
     * Useful for monitoring and testing.
     */
    fun getCacheSize(): Int = cache.size

    /**
     * Clears all cached buckets.
     * Useful for testing and dynamic configuration reloading.
     */
    fun clearCache() {
        cache.clear()
        metrics.updateCacheSize(0)
        logger.debug("Cleared all cached buckets")
    }
}
