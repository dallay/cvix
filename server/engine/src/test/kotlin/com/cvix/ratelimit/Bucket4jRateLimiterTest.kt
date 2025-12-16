package com.cvix.ratelimit

import com.cvix.ratelimit.domain.RateLimitResult
import com.cvix.ratelimit.domain.RateLimitStrategy
import com.cvix.ratelimit.infrastructure.adapter.Bucket4jRateLimiter
import com.cvix.ratelimit.infrastructure.config.BucketConfigurationFactory
import com.cvix.ratelimit.infrastructure.config.RateLimitProperties
import com.cvix.ratelimit.infrastructure.metrics.RateLimitMetrics
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.time.Clock
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier

/**
 * Unit tests for Bucket4jRateLimiter.
 *
 * Tests cover:
 * - Token consumption for AUTH and BUSINESS strategies
 * - Rate limit denial and allowed scenarios
 * - Bucket caching per identifier and strategy
 * - Correct mapping to RateLimitResult
 * - Token refill behavior
 */
class Bucket4jRateLimiterTest {

    private lateinit var rateLimiter: Bucket4jRateLimiter
    private lateinit var properties: RateLimitProperties

    @BeforeEach
    fun setUp() {
        properties = RateLimitProperties(
            enabled = true,
            auth = RateLimitProperties.AuthRateLimitConfig(
                enabled = true,
                limits = listOf(
                    RateLimitProperties.BandwidthLimit(
                        name = "per-minute",
                        capacity = 5, // Small capacity for testing
                        refillTokens = 5,
                        refillDuration = Duration.ofMinutes(1),
                    ),
                ),
            ),
            business = RateLimitProperties.BusinessRateLimitConfig(
                enabled = true,
                pricingPlans = mapOf(
                    "free" to RateLimitProperties.BandwidthLimit(
                        name = "free-plan",
                        capacity = 3, // Small capacity for testing
                        refillTokens = 3,
                        refillDuration = Duration.ofHours(1),
                    ),
                    "basic" to RateLimitProperties.BandwidthLimit(
                        name = "basic-plan",
                        capacity = 5,
                        refillTokens = 5,
                        refillDuration = Duration.ofHours(1),
                    ),
                    "professional" to RateLimitProperties.BandwidthLimit(
                        name = "professional-plan",
                        capacity = 10,
                        refillTokens = 10,
                        refillDuration = Duration.ofHours(1),
                    ),
                ),
            ),
        )
        val configFactory = BucketConfigurationFactory(properties)
        val meterRegistry = SimpleMeterRegistry()
        val metrics = RateLimitMetrics(meterRegistry)
        // Use system clock by default for existing tests
        rateLimiter = Bucket4jRateLimiter(configFactory, metrics)
    }

    @Test
    fun `should allow token consumption when under limit for AUTH strategy`() {
        // Given
        val identifier = "IP:192.168.1.1"

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 4 // 5 capacity - 1 consumed
                result.limitCapacity shouldBe 5
                result.resetTime.epochSecond shouldBeGreaterThanOrEqual 0
            }
            .verifyComplete()
    }

    @Test
    fun `should deny token consumption when limit exceeded for AUTH strategy`() {
        // Given
        val identifier = "IP:192.168.1.2"

        // Consume all tokens
        repeat(5) {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH).block()
        }

        // When/Then - next request should be denied
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Denied>()
                result.retryAfter.seconds shouldBeGreaterThanOrEqual 0
                result.limitCapacity shouldBe 5
            }
            .verifyComplete()
    }

    @Test
    fun `should allow token consumption when under limit for BUSINESS strategy with FREE plan`() {
        // Given
        val identifier = "FREE-KEY-123"

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 2 // 3 capacity - 1 consumed
                result.limitCapacity shouldBe 3
                result.resetTime.epochSecond shouldBeGreaterThanOrEqual 0
            }
            .verifyComplete()
    }

    @Test
    fun `should deny token consumption when limit exceeded for BUSINESS strategy`() {
        // Given
        val identifier = "FREE-KEY-456"

        // Consume all tokens
        repeat(3) {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS).block()
        }

        // When/Then - next request should be denied
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Denied>()
                result.retryAfter.seconds shouldBeGreaterThanOrEqual 0
                result.limitCapacity shouldBe 3
            }
            .verifyComplete()
    }

    @Test
    fun `should allow token consumption for BASIC plan`() {
        // Given
        val identifier = "BX001-BASIC-KEY"

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 4 // 5 capacity - 1 consumed
                result.limitCapacity shouldBe 5
                result.resetTime.epochSecond shouldBeGreaterThanOrEqual 0
            }
            .verifyComplete()
    }

    @Test
    fun `should allow token consumption for PROFESSIONAL plan`() {
        // Given
        val identifier = "PX001-PRO-KEY"

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 9 // 10 capacity - 1 consumed
                result.limitCapacity shouldBe 10
                result.resetTime.epochSecond shouldBeGreaterThanOrEqual 0
            }
            .verifyComplete()
    }

    @Test
    fun `should maintain separate buckets for different identifiers with same strategy`() {
        // Given
        val identifier1 = "IP:192.168.1.1"
        val identifier2 = "IP:192.168.1.2"

        // When - consume tokens for identifier1
        repeat(5) {
            rateLimiter.consumeToken(identifier1, RateLimitStrategy.AUTH).block()
        }

        // Then - identifier2 should still have tokens available
        StepVerifier.create(rateLimiter.consumeToken(identifier2, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
            }
            .verifyComplete()
    }

    @Test
    fun `should maintain separate buckets for same identifier with different strategies`() {
        // Given
        val identifier = "TEST-KEY"

        // When - consume all AUTH tokens
        repeat(5) {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH).block()
        }

        // Then - BUSINESS strategy should still have tokens available
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
            }
            .verifyComplete()
    }

    @Test
    fun `should decrement remaining tokens with each consumption`() {
        // Given
        val identifier = "IP:192.168.1.3"

        // When/Then - first consumption
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 4
                result.limitCapacity shouldBe 5
            }
            .verifyComplete()

        // When/Then - second consumption
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 3
                result.limitCapacity shouldBe 5
            }
            .verifyComplete()

        // When/Then - third consumption
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 2
                result.limitCapacity shouldBe 5
            }
            .verifyComplete()
    }

    @Test
    fun `should use default BUSINESS strategy when calling consumeToken with identifier only`() {
        // Given
        val identifier = "DEFAULT-KEY"

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(identifier))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                // Should use FREE plan (3 tokens) as default
                result.remainingTokens shouldBe 2
                result.limitCapacity shouldBe 3
            }
            .verifyComplete()
    }

    @Test
    fun `should cache buckets for repeated requests`() {
        // Given
        val identifier = "CACHED-KEY"

        // When - make multiple requests
        val result1 = rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH).block()
        val result2 = rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH).block()

        // Then - should see token decrements indicating same bucket is being used
        result1.shouldBeInstanceOf<RateLimitResult.Allowed>()
        result1.remainingTokens shouldBe 4
        result1.limitCapacity shouldBe 5

        result2.shouldBeInstanceOf<RateLimitResult.Allowed>()
        result2.remainingTokens shouldBe 3
        result2.limitCapacity shouldBe 5
    }

    @Test
    fun `should handle IP-based identifiers correctly`() {
        // Given
        val ipIdentifier = "IP:10.0.0.1"

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(ipIdentifier, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.limitCapacity shouldBe 5
            }
            .verifyComplete()
    }

    @Test
    fun `should handle API key identifiers correctly`() {
        // Given
        val apiKeyIdentifier = "PX001-ABC123"

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(apiKeyIdentifier, RateLimitStrategy.BUSINESS))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 9 // Professional plan (10 tokens)
                result.limitCapacity shouldBe 10
            }
            .verifyComplete()
    }

    @Test
    fun `should return non-negative retry after duration when denied`() {
        // Given
        val identifier = "RETRY-TEST"

        // Consume all tokens
        repeat(5) {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH).block()
        }

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Denied>()
                result.retryAfter.isNegative shouldBe false
                result.retryAfter.isZero shouldBe false
                result.limitCapacity shouldBe 5
            }
            .verifyComplete()
    }

    // Option A: Tolerant test - assert resetTime is between now and now + refillDuration + tolerance
    @Test
    fun `reset time is within expected range (tolerant)`() {
        val identifier = "TOLERANT-TEST"
        val now = Instant.now()
        val result = rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH)
            .block() as RateLimitResult.Allowed

        // refillDuration configured for AUTH is 1 minute
        val upperBound = now.plus(Duration.ofMinutes(1)).plusMillis(1000) // 1s tolerance
        assert(!result.resetTime.isBefore(now)) { "resetTime should not be before now" }
        assert(!result.resetTime.isAfter(upperBound)) { "resetTime should be within now + refillDuration + tolerance" }
    }

    // Option B: Deterministic test by injecting a fixed clock
    @Test
    fun `reset time deterministic with injected clock`() {
        val fixedNow = Instant.parse("2025-01-01T00:00:00Z")
        val fixedClock = Clock.fixed(fixedNow, java.time.ZoneOffset.UTC)
        val configFactory = BucketConfigurationFactory(properties)
        val meterRegistry = SimpleMeterRegistry()
        val metrics = RateLimitMetrics(meterRegistry)
        val deterministicLimiter = Bucket4jRateLimiter(configFactory, metrics, fixedClock)

        val result = deterministicLimiter.consumeToken("DETERMINISTIC-KEY", RateLimitStrategy.AUTH)
            .block() as RateLimitResult.Allowed
        val expectedReset = fixedNow.plus(Duration.ofMinutes(1))
        // Allow slight tolerance for internal timing, but deterministic clock should make this exact
        assert(result.resetTime == expectedReset) { "expected reset time $expectedReset but was ${result.resetTime}" }
    }
}
