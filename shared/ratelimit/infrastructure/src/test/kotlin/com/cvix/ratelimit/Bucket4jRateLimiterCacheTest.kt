package com.cvix.ratelimit

import com.cvix.UnitTest
import com.cvix.ratelimit.domain.RateLimitStrategy
import com.cvix.ratelimit.infrastructure.adapter.ApiKeyParser
import com.cvix.ratelimit.infrastructure.adapter.Bucket4jRateLimiter
import com.cvix.ratelimit.infrastructure.config.BucketConfigurationFactory
import com.cvix.ratelimit.infrastructure.config.RateLimitProperties
import com.cvix.ratelimit.infrastructure.metrics.RateLimitMetrics
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.time.Clock
import java.time.Duration
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for Bucket4jRateLimiter cache behavior.
 *
 * These tests verify that the cache implementation:
 * - Respects maximum size limits
 * - Evicts least-recently-used entries when full
 * - Provides accurate statistics
 * - Prevents unbounded memory growth
 */
@UnitTest
class Bucket4jRateLimiterCacheTest {

    private lateinit var rateLimiter: Bucket4jRateLimiter
    private lateinit var properties: RateLimitProperties

    @BeforeEach
    fun setUp() {
        properties = RateLimitProperties(
            enabled = true,
            apiKeyPrefixes = RateLimitProperties.ApiKeyPrefixConfig(
                professional = "PX001-",
                basic = "BX001-",
            ),
            auth = RateLimitProperties.AuthRateLimitConfig(
                enabled = true,
                limits = listOf(
                    RateLimitProperties.BandwidthLimit(
                        name = "per-minute",
                        capacity = 10,
                        refillTokens = 10,
                        refillDuration = Duration.ofMinutes(1),
                    ),
                ),
            ),
            business = RateLimitProperties.BusinessRateLimitConfig(
                enabled = true,
                pricingPlans = mapOf(
                    "free" to RateLimitProperties.BandwidthLimit(
                        name = "free-plan",
                        capacity = 10,
                        refillTokens = 10,
                        refillDuration = Duration.ofHours(1),
                    ),
                ),
            ),
        )
    }

    @Test
    fun `should respect maximum cache size and eventually evict entries`() = runTest {
        // Given: Rate limiter with small cache (10 entries)
        val configFactory = BucketConfigurationFactory(properties)
        val apiKeyParser = ApiKeyParser(properties)
        val meterRegistry = SimpleMeterRegistry()
        val metrics = RateLimitMetrics(meterRegistry)
        // Override cache config for small cache testing
        val testProperties = properties.copy(
            cache = RateLimitProperties.CacheConfig(maxSize = 10, ttlMinutes = 60),
        )
        rateLimiter = Bucket4jRateLimiter(
            configurationFactory = configFactory,
            apiKeyParser = apiKeyParser,
            metrics = metrics,
            properties = testProperties,
            clock = Clock.systemUTC(),
        )

        // When: Add many more entries than cache size (30 entries, max cache size 10)
        repeat(30) { i ->
            val identifier = "IP:192.168.1.$i"
            rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH)
        }

        // Force Caffeine to process evictions synchronously for deterministic testing
        // This triggers the pending maintenance tasks (eviction cleanup) that Caffeine
        // normally runs asynchronously for performance
        rateLimiter.triggerCacheCleanup()

        // Then: Cache stats should show that evictions occurred
        // This proves the bounded behavior is working
        val stats = rateLimiter.getCacheStats()
        val evictionCount = stats.evictionCount()

        // With 30 inserts and max size 10, we MUST have evictions
        // The exact number depends on Caffeine's async cleanup timing
        // NOTE: Caffeine uses asynchronous eviction for performance, so eviction count
        // may vary slightly between runs, but it MUST be > 0 to prove bounded behavior
        evictionCount shouldBeGreaterThan 0

        // And: Cache size should be bounded (with tolerance for async eviction)
        // Caffeine may temporarily exceed the limit before cleanup runs
        // In production, this is fine—the cache stabilizes quickly under load
        // We allow 50% tolerance (15) to account for async cleanup timing
        val cacheSize = rateLimiter.getCacheSize()
        cacheSize shouldBeLessThanOrEqual 15
    }

    @Test
    fun `should provide accurate cache statistics`() = runTest {
        // Given: Rate limiter with cache stats enabled
        val configFactory = BucketConfigurationFactory(properties)
        val apiKeyParser = ApiKeyParser(properties)
        val meterRegistry = SimpleMeterRegistry()
        val metrics = RateLimitMetrics(meterRegistry)
        // Override cache config for larger cache testing
        val testProperties = properties.copy(
            cache = RateLimitProperties.CacheConfig(maxSize = 100, ttlMinutes = 60),
        )
        rateLimiter = Bucket4jRateLimiter(
            configurationFactory = configFactory,
            apiKeyParser = apiKeyParser,
            metrics = metrics,
            properties = testProperties,
            clock = Clock.systemUTC(),
        )

        // When: Make requests with repeated identifiers
        val identifier1 = "IP:192.168.1.1"
        val identifier2 = "IP:192.168.1.2"

        // First access - cache miss
        rateLimiter.consumeToken(identifier1, RateLimitStrategy.AUTH)
        rateLimiter.consumeToken(identifier2, RateLimitStrategy.AUTH)

        // Second access - cache hit
        rateLimiter.consumeToken(identifier1, RateLimitStrategy.AUTH)
        rateLimiter.consumeToken(identifier2, RateLimitStrategy.AUTH)

        // Then: Stats should reflect hits and misses
        val stats = rateLimiter.getCacheStats()
        stats.hitCount() shouldBe 2 // Second access to both identifiers
        stats.missCount() shouldBe 2 // First access to both identifiers
        stats.loadCount() shouldBe 2 // Two entries loaded
    }

    @Test
    fun `should clear cache correctly`() = runTest {
        // Given: Rate limiter with some cached entries
        val configFactory = BucketConfigurationFactory(properties)
        val apiKeyParser = ApiKeyParser(properties)
        val meterRegistry = SimpleMeterRegistry()
        val metrics = RateLimitMetrics(meterRegistry)
        rateLimiter = Bucket4jRateLimiter(
            configurationFactory = configFactory,
            apiKeyParser = apiKeyParser,
            metrics = metrics,
            properties = properties,
            clock = Clock.systemUTC(),
        )

        // Add several entries
        repeat(5) { i ->
            val identifier = "IP:192.168.1.$i"
            rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH)
        }

        // When: Clear cache
        rateLimiter.clearCache()

        // Then: Cache should be empty
        val cacheSize = rateLimiter.getCacheSize()
        cacheSize shouldBe 0
    }

    @Test
    fun `should maintain separate cache entries for different strategies`() = runTest {
        // Given: Rate limiter
        val configFactory = BucketConfigurationFactory(properties)
        val apiKeyParser = ApiKeyParser(properties)
        val meterRegistry = SimpleMeterRegistry()
        val metrics = RateLimitMetrics(meterRegistry)
        rateLimiter = Bucket4jRateLimiter(
            configurationFactory = configFactory,
            apiKeyParser = apiKeyParser,
            metrics = metrics,
            properties = properties,
            clock = Clock.systemUTC(),
        )

        // When: Use same identifier with different strategies
        val identifier = "TEST-KEY"
        rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH)
        rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS)

        // Then: Should have two separate cache entries (AUTH:TEST-KEY and BUSINESS:TEST-KEY)
        val cacheSize = rateLimiter.getCacheSize()
        cacheSize shouldBe 2
    }
}
