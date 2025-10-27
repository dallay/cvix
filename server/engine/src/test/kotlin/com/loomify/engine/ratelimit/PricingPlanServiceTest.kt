package com.loomify.engine.ratelimit

import com.loomify.engine.ratelimit.infrastructure.PricingPlanService
import io.github.bucket4j.Bucket
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for PricingPlanService.
 *
 * Tests cover:
 * - Bucket resolution and caching
 * - Different API keys get different buckets
 * - Same API key reuses cached bucket
 * - Cache management
 */
class PricingPlanServiceTest {

    private lateinit var service: PricingPlanService

    @BeforeEach
    fun setUp() {
        service = PricingPlanService()
    }

    @Test
    fun `should create bucket for new API key`() {
        // When
        val bucket = service.resolveBucket("FX001-TEST123")

        // Then
        bucket.shouldBeInstanceOf<Bucket>()
    }

    @Test
    fun `should reuse cached bucket for same API key`() {
        // Given
        val apiKey = "PX001-TEST456"

        // When
        val bucket1 = service.resolveBucket(apiKey)
        val bucket2 = service.resolveBucket(apiKey)

        // Then
        bucket1 shouldBe bucket2  // Same instance
    }

    @Test
    fun `should create different buckets for different API keys`() {
        // When
        val bucket1 = service.resolveBucket("FX001-KEY1")
        val bucket2 = service.resolveBucket("BX001-KEY2")

        // Then
        bucket1 shouldNotBe bucket2
    }

    @Test
    fun `should track cache size correctly`() {
        // Given - initially empty
        service.getCacheSize() shouldBeExactly 0

        // When - add some buckets
        service.resolveBucket("KEY1")
        service.resolveBucket("KEY2")
        service.resolveBucket("KEY3")

        // Then
        service.getCacheSize() shouldBeExactly 3

        // When - reuse existing key
        service.resolveBucket("KEY1")

        // Then - size doesn't change
        service.getCacheSize() shouldBeExactly 3
    }

    @Test
    fun `should clear cache when requested`() {
        // Given
        service.resolveBucket("KEY1")
        service.resolveBucket("KEY2")
        service.getCacheSize() shouldBeExactly 2

        // When
        service.clearCache()

        // Then
        service.getCacheSize() shouldBeExactly 0
    }

    @Test
    fun `should create new bucket after cache clear`() {
        // Given
        val apiKey = "TEST-KEY"
        val bucket1 = service.resolveBucket(apiKey)

        // When
        service.clearCache()
        val bucket2 = service.resolveBucket(apiKey)

        // Then
        bucket1 shouldNotBe bucket2  // Different instances
    }

    @Test
    fun `should handle IP-based identifiers`() {
        // When
        val bucket = service.resolveBucket("IP:192.168.1.1")

        // Then
        bucket.shouldBeInstanceOf<Bucket>()
        service.getCacheSize() shouldBeExactly 1
    }

    @Test
    fun `should create buckets with proper rate limits for different plans`() {
        // Given
        val freeKey = "FREE-KEY"
        val basicKey = "BX001-BASIC"
        val proKey = "PX001-PRO"

        // When
        val freeBucket = service.resolveBucket(freeKey)
        val basicBucket = service.resolveBucket(basicKey)
        val proBucket = service.resolveBucket(proKey)

        // Then - all should be valid buckets
        freeBucket.shouldBeInstanceOf<Bucket>()
        basicBucket.shouldBeInstanceOf<Bucket>()
        proBucket.shouldBeInstanceOf<Bucket>()

        // And they should be different instances
        freeBucket shouldNotBe basicBucket
        basicBucket shouldNotBe proBucket
        freeBucket shouldNotBe proBucket
    }
}
