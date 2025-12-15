package com.cvix.ratelimit.infrastructure

import com.cvix.ratelimit.domain.RateLimitResult
import com.cvix.ratelimit.domain.RateLimitStrategy
import com.cvix.ratelimit.domain.RateLimiter
import com.cvix.ratelimit.infrastructure.config.BucketConfigurationStrategy
import io.github.bucket4j.Bucket
import io.github.bucket4j.ConsumptionProbe
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * Adapter that implements the RateLimiterPort using Bucket4j.
 * This class is responsible for the actual rate limiting logic using the Bucket4j library.
 * Operations are executed on a bounded elastic scheduler to avoid blocking the reactive pipeline.
 *
 * This implementation supports two strategies:
 * - AUTH: For authentication endpoints, using time-based limits (per-minute, per-hour)
 * - BUSINESS: For business endpoints, using pricing plan-based limits
 *
 * @property configurationStrategy Strategy for building bucket configurations from properties.
 */
@Component
class Bucket4jRateLimiter(
    private val configurationStrategy: BucketConfigurationStrategy
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
            val cacheKey = "${strategy.name}:$identifier"
            val bucket = cache.computeIfAbsent(cacheKey) { newBucket(identifier, strategy) }
            val probe: ConsumptionProbe = bucket.tryConsumeAndReturnRemaining(1)

            if (probe.isConsumed) {
                logger.debug(
                    "Token consumed for identifier: {}, strategy: {}, remaining: {}",
                    identifier, strategy, probe.remainingTokens,
                )
                RateLimitResult.Allowed(probe.remainingTokens)
            } else {
                val retryAfter = Duration.ofNanos(probe.nanosToWaitForRefill)
                logger.warn(
                    "Rate limit exceeded for identifier: {}, strategy: {}, retry after: {}",
                    identifier, strategy, retryAfter,
                )
                RateLimitResult.Denied(retryAfter)
            }
        }.subscribeOn(Schedulers.boundedElastic())
    }

    private fun newBucket(identifier: String, strategy: RateLimitStrategy): Bucket {
        val configuration = when (strategy) {
            RateLimitStrategy.AUTH -> {
                logger.debug("Creating AUTH bucket for identifier: {}", identifier)
                configurationStrategy.createAuthBucketConfiguration()
            }

            RateLimitStrategy.BUSINESS -> {
                val pricingPlan = PricingPlan.resolvePlanFromApiKey(identifier)
                logger.debug(
                    "Creating BUSINESS bucket for identifier: {} with plan: {}",
                    identifier,
                    pricingPlan.name,
                )
                configurationStrategy.createBusinessBucketConfiguration(pricingPlan.name.lowercase())
            }

            RateLimitStrategy.RESUME -> {
                logger.debug("Creating RESUME bucket for identifier: {}", identifier)
                configurationStrategy.createResumeBucketConfiguration()
            }

            RateLimitStrategy.WAITLIST -> {
                logger.debug("Creating WAITLIST bucket for identifier: {}", identifier)
                configurationStrategy.createWaitlistBucketConfiguration()
            }
        }

        // Build bucket with the configured limits
        val builder = Bucket.builder()
        configuration.bandwidths.forEach { bandwidth ->
            builder.addLimit(bandwidth)
        }
        return builder.build()
    }
}
