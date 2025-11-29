package com.loomify.ratelimit

import com.loomify.ratelimit.infrastructure.config.BucketConfigurationStrategy
import com.loomify.ratelimit.infrastructure.config.RateLimitProperties
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.time.Duration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for BucketConfigurationStrategy.
 *
 * Tests cover:
 * - Auth bucket configuration creation
 * - Business bucket configuration creation
 * - Multiple limit handling
 * - Invalid plan handling
 * - Enable/disable flags
 */
class BucketConfigurationStrategyTest {

    private lateinit var strategy: BucketConfigurationStrategy
    private lateinit var properties: RateLimitProperties

    @BeforeEach
    fun setUp() {
        properties = RateLimitProperties(
            enabled = true,
            auth = RateLimitProperties.AuthRateLimitConfig(
                enabled = true,
                endpoints = listOf("/api/auth/login", "/api/auth/register"),
                limits = listOf(
                    RateLimitProperties.BandwidthLimit(
                        name = "per-minute",
                        capacity = 10,
                        refillTokens = 10,
                        refillDuration = Duration.ofMinutes(1),
                    ),
                    RateLimitProperties.BandwidthLimit(
                        name = "per-hour",
                        capacity = 100,
                        refillTokens = 100,
                        refillDuration = Duration.ofHours(1),
                    ),
                ),
            ),
            business = RateLimitProperties.BusinessRateLimitConfig(
                enabled = true,
                pricingPlans = mapOf(
                    "free" to RateLimitProperties.BandwidthLimit(
                        name = "free-plan",
                        capacity = 20,
                        refillTokens = 20,
                        refillDuration = Duration.ofHours(1),
                    ),
                    "basic" to RateLimitProperties.BandwidthLimit(
                        name = "basic-plan",
                        capacity = 40,
                        refillTokens = 40,
                        refillDuration = Duration.ofHours(1),
                    ),
                    "professional" to RateLimitProperties.BandwidthLimit(
                        name = "professional-plan",
                        capacity = 100,
                        refillTokens = 100,
                        refillDuration = Duration.ofHours(1),
                    ),
                ),
            ),
        )
        strategy = BucketConfigurationStrategy(properties)
    }

    @Test
    fun `should create auth bucket configuration with multiple limits`() {
        // When
        val config = strategy.createAuthBucketConfiguration()

        // Then
        config.bandwidths.size shouldBe 2
    }

    @Test
    fun `should create business bucket configuration for free plan`() {
        // When
        val config = strategy.createBusinessBucketConfiguration("free")

        // Then
        config.bandwidths.size shouldBe 1
    }

    @Test
    fun `should create business bucket configuration for basic plan`() {
        // When
        val config = strategy.createBusinessBucketConfiguration("basic")

        // Then
        config.bandwidths.size shouldBe 1
    }

    @Test
    fun `should create business bucket configuration for professional plan`() {
        // When
        val config = strategy.createBusinessBucketConfiguration("professional")

        // Then
        config.bandwidths.size shouldBe 1
    }

    @Test
    fun `should be case-insensitive for plan names`() {
        // When
        val config1 = strategy.createBusinessBucketConfiguration("FREE")
        val config2 = strategy.createBusinessBucketConfiguration("free")
        val config3 = strategy.createBusinessBucketConfiguration("Free")

        // Then - all should succeed
        config1.bandwidths.size shouldBe 1
        config2.bandwidths.size shouldBe 1
        config3.bandwidths.size shouldBe 1
    }

    @Test
    fun `should throw exception for unknown pricing plan`() {
        // When/Then
        val exception = shouldThrow<IllegalArgumentException> {
            strategy.createBusinessBucketConfiguration("unknown-plan")
        }

        exception.message shouldContain "Unknown pricing plan"
        exception.message shouldContain "unknown-plan"
        exception.message shouldContain "free"
        exception.message shouldContain "basic"
        exception.message shouldContain "professional"
    }

    @Test
    fun `should return auth endpoints list`() {
        // When
        val endpoints = strategy.getAuthEndpoints()

        // Then
        endpoints.size shouldBe 2
        endpoints shouldContain "/api/auth/login"
        endpoints shouldContain "/api/auth/register"
    }

    @Test
    fun `should return true when auth rate limiting is fully enabled`() {
        // When
        val isEnabled = strategy.isAuthRateLimitEnabled()

        // Then
        isEnabled shouldBe true
    }

    @Test
    fun `should return false when auth rate limiting is disabled globally`() {
        // Given
        val disabledProperties = properties.copy(enabled = false)
        val disabledStrategy = BucketConfigurationStrategy(disabledProperties)

        // When
        val isEnabled = disabledStrategy.isAuthRateLimitEnabled()

        // Then
        isEnabled shouldBe false
    }

    @Test
    fun `should return false when auth rate limiting is disabled specifically`() {
        // Given
        val disabledAuthProperties = properties.copy(
            auth = properties.auth.copy(enabled = false),
        )
        val disabledAuthStrategy = BucketConfigurationStrategy(disabledAuthProperties)

        // When
        val isEnabled = disabledAuthStrategy.isAuthRateLimitEnabled()

        // Then
        isEnabled shouldBe false
    }

    @Test
    fun `should return true when business rate limiting is fully enabled`() {
        // When
        val isEnabled = strategy.isBusinessRateLimitEnabled()

        // Then
        isEnabled shouldBe true
    }

    @Test
    fun `should return false when business rate limiting is disabled globally`() {
        // Given
        val disabledProperties = properties.copy(enabled = false)
        val disabledStrategy = BucketConfigurationStrategy(disabledProperties)

        // When
        val isEnabled = disabledStrategy.isBusinessRateLimitEnabled()

        // Then
        isEnabled shouldBe false
    }

    @Test
    fun `should return false when business rate limiting is disabled specifically`() {
        // Given
        val disabledBusinessProperties = properties.copy(
            business = properties.business.copy(enabled = false),
        )
        val disabledBusinessStrategy = BucketConfigurationStrategy(disabledBusinessProperties)

        // When
        val isEnabled = disabledBusinessStrategy.isBusinessRateLimitEnabled()

        // Then
        isEnabled shouldBe false
    }

    @Test
    fun `should handle empty auth limits list gracefully`() {
        // Given
        val emptyLimitsProperties = properties.copy(
            auth = properties.auth.copy(limits = emptyList()),
        )
        val emptyLimitsStrategy = BucketConfigurationStrategy(emptyLimitsProperties)

        // When/Then - Bucket4j doesn't allow empty configuration, so it should throw
        shouldThrow<IllegalArgumentException> {
            emptyLimitsStrategy.createAuthBucketConfiguration()
        }
    }

    @Test
    fun `should handle single auth limit`() {
        // Given
        val singleLimitProperties = properties.copy(
            auth = properties.auth.copy(
                limits = listOf(
                    RateLimitProperties.BandwidthLimit(
                        name = "per-minute",
                        capacity = 10,
                        refillTokens = 10,
                        refillDuration = Duration.ofMinutes(1),
                    ),
                ),
            ),
        )
        val singleLimitStrategy = BucketConfigurationStrategy(singleLimitProperties)

        // When
        val config = singleLimitStrategy.createAuthBucketConfiguration()

        // Then
        config.bandwidths.size shouldBe 1
    }

    @Test
    fun `should create bucket with custom initial tokens`() {
        // Given
        val customProperties = properties.copy(
            business = RateLimitProperties.BusinessRateLimitConfig(
                enabled = true,
                pricingPlans = mapOf(
                    "custom" to RateLimitProperties.BandwidthLimit(
                        name = "custom-plan",
                        capacity = 100,
                        refillTokens = 50,
                        refillDuration = Duration.ofHours(1),
                        initialTokens = 10,
                    ),
                ),
            ),
        )
        val customStrategy = BucketConfigurationStrategy(customProperties)

        // When
        val config = customStrategy.createBusinessBucketConfiguration("custom")

        // Then
        config.bandwidths.size shouldBe 1
    }
}
