package com.loomify.engine.ratelimit.infrastructure

import io.github.bucket4j.Bucket
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Service for managing rate limiting buckets per API key.
 *
 * This service maintains an in-memory cache of Bucket4j buckets, one per unique API key.
 * Buckets are created on-demand and cached for subsequent requests.
 *
 * **Note**: This implementation uses an in-memory ConcurrentHashMap suitable for
 * single-instance deployments. For distributed deployments, consider using
 * Bucket4j's JCache integration with Redis or Hazelcast.
 *
 * Based on: https://www.baeldung.com/spring-bucket4j
 *
 * @since 2.0.0
 */
@Service
class PricingPlanService {
    private val logger = LoggerFactory.getLogger(PricingPlanService::class.java)

    /**
     * In-memory cache of buckets per API key.
     * Key: API key string
     * Value: Bucket4j Bucket instance
     */
    private val cache: MutableMap<String, Bucket> = ConcurrentHashMap()

    /**
     * Resolves and returns the rate limiting bucket for an API key.
     * Creates a new bucket if one doesn't exist for the given key.
     *
     * @param apiKey The API key to resolve the bucket for
     * @return The Bucket4j bucket for this API key
     */
    fun resolveBucket(apiKey: String): Bucket {
        return cache.computeIfAbsent(apiKey) { key ->
            logger.debug("Creating new bucket for API key: ${key.take(10)}...")
            newBucket(key)
        }
    }

    /**
     * Creates a new bucket for an API key based on its pricing plan.
     *
     * @param apiKey The API key to create a bucket for
     * @return A new Bucket4j bucket with appropriate rate limits
     */
    private fun newBucket(apiKey: String): Bucket {
        val pricingPlan = PricingPlan.resolvePlanFromApiKey(apiKey)
        logger.info("Resolved pricing plan $pricingPlan for API key ${apiKey.take(10)}...")

        return Bucket.builder()
            .addLimit(pricingPlan.getLimit())
            .build()
    }

    /**
     * Returns the number of cached buckets.
     * Useful for monitoring and testing.
     */
    fun getCacheSize(): Int = cache.size

    /**
     * Clears all cached buckets.
     * Useful for testing.
     */
    fun clearCache() {
        cache.clear()
        logger.debug("Cleared all cached buckets")
    }
}
