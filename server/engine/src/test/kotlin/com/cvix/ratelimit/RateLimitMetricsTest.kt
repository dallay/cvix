package com.cvix.ratelimit

import com.cvix.UnitTest
import com.cvix.ratelimit.domain.RateLimitResult
import com.cvix.ratelimit.domain.RateLimitStrategy
import com.cvix.ratelimit.infrastructure.metrics.RateLimitMetrics
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for RateLimitMetrics.
 *
 * Tests cover:
 * - Recording allowed and denied rate limit checks
 * - Counter increments for different strategies
 * - Cache size gauge updates
 * - Timer recording for token consumption
 */
@UnitTest
class RateLimitMetricsTest {

    private lateinit var meterRegistry: MeterRegistry
    private lateinit var metrics: RateLimitMetrics

    @BeforeEach
    fun setUp() {
        meterRegistry = SimpleMeterRegistry()
        metrics = RateLimitMetrics(meterRegistry)
    }

    @Test
    fun `should record allowed rate limit check`() {
        // Given
        val strategy = RateLimitStrategy.AUTH
        val result = RateLimitResult.Allowed(
            remainingTokens = 9,
            limitCapacity = 10,
            resetTime = Instant.now().plusSeconds(60),
        )

        // When
        metrics.recordRateLimitCheck(strategy, result)

        // Then
        val counter = meterRegistry.counter(
            "rate_limit.requests.total",
            "strategy", "auth",
            "result", "allowed",
        )
        counter.count() shouldBe 1.0
    }

    @Test
    fun `should record denied rate limit check`() {
        // Given
        val strategy = RateLimitStrategy.RESUME
        val result = RateLimitResult.Denied(
            retryAfter = Duration.ofMinutes(1),
            limitCapacity = 10,
        )

        // When
        metrics.recordRateLimitCheck(strategy, result)

        // Then
        val requestsCounter = meterRegistry.counter(
            "rate_limit.requests.total",
            "strategy", "resume",
            "result", "denied",
        )
        requestsCounter.count() shouldBe 1.0

        val deniedCounter = meterRegistry.counter(
            "rate_limit.denied.total",
            "strategy", "resume",
        )
        deniedCounter.count() shouldBe 1.0
    }

    @Test
    fun `should increment counters for multiple checks`() {
        // Given
        val strategy = RateLimitStrategy.BUSINESS
        val allowedResult = RateLimitResult.Allowed(
            remainingTokens = 5,
            limitCapacity = 10,
            resetTime = Instant.now().plusSeconds(3600),
        )
        val deniedResult = RateLimitResult.Denied(
            retryAfter = Duration.ofHours(1),
            limitCapacity = 10,
        )

        // When
        repeat(3) { metrics.recordRateLimitCheck(strategy, allowedResult) }
        repeat(2) { metrics.recordRateLimitCheck(strategy, deniedResult) }

        // Then
        val allowedCounter = meterRegistry.counter(
            "rate_limit.requests.total",
            "strategy", "business",
            "result", "allowed",
        )
        allowedCounter.count() shouldBe 3.0

        val deniedCounter = meterRegistry.counter(
            "rate_limit.requests.total",
            "strategy", "business",
            "result", "denied",
        )
        deniedCounter.count() shouldBe 2.0

        val totalDenied = meterRegistry.counter(
            "rate_limit.denied.total",
            "strategy", "business",
        )
        totalDenied.count() shouldBe 2.0
    }

    @Test
    fun `should track different strategies separately`() {
        // Given
        val authResult = RateLimitResult.Allowed(9, 10, Instant.now().plusSeconds(60))
        val businessResult = RateLimitResult.Allowed(19, 20, Instant.now().plusSeconds(3600))

        // When
        metrics.recordRateLimitCheck(RateLimitStrategy.AUTH, authResult)
        metrics.recordRateLimitCheck(RateLimitStrategy.BUSINESS, businessResult)

        // Then
        val authCounter = meterRegistry.counter(
            "rate_limit.requests.total",
            "strategy", "auth",
            "result", "allowed",
        )
        authCounter.count() shouldBe 1.0

        val businessCounter = meterRegistry.counter(
            "rate_limit.requests.total",
            "strategy", "business",
            "result", "allowed",
        )
        businessCounter.count() shouldBe 1.0
    }

    @Test
    fun `should update cache size gauge`() {
        // When
        metrics.updateCacheSize(42)

        // Then
        val gauge = meterRegistry.find("rate_limit.cache.size").gauge()
        gauge?.value() shouldBe 42.0
    }

    @Test
    fun `should update cache size gauge multiple times`() {
        // When
        metrics.updateCacheSize(10)
        metrics.updateCacheSize(20)
        metrics.updateCacheSize(5)

        // Then
        val gauge = meterRegistry.find("rate_limit.cache.size").gauge()
        gauge?.value() shouldBe 5.0
    }

    @Test
    fun `should record token consumption time`() {
        // Given
        val strategy = RateLimitStrategy.WAITLIST

        // When
        val result = metrics.recordTokenConsumption(strategy) {
            Thread.sleep(10) // Simulate some work
            "test-result"
        }

        // Then
        result shouldBe "test-result"

        val timer = meterRegistry.timer(
            "rate_limit.token.consumption.time",
            "strategy", "waitlist",
        )
        timer.count() shouldBe 1L
        timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS) shouldBeGreaterThan 0.0
    }

    @Test
    fun `should record multiple token consumptions for same strategy`() {
        // Given
        val strategy = RateLimitStrategy.AUTH

        // When
        repeat(5) {
            metrics.recordTokenConsumption(strategy) {
                "result-$it"
            }
        }

        // Then
        val timer = meterRegistry.timer(
            "rate_limit.token.consumption.time",
            "strategy", "auth",
        )
        timer.count() shouldBe 5L
    }

    @Test
    fun `should track token consumption for different strategies separately`() {
        // When
        metrics.recordTokenConsumption(RateLimitStrategy.AUTH) { "auth" }
        metrics.recordTokenConsumption(RateLimitStrategy.BUSINESS) { "business" }
        metrics.recordTokenConsumption(RateLimitStrategy.AUTH) { "auth-2" }

        // Then
        val authTimer = meterRegistry.timer(
            "rate_limit.token.consumption.time",
            "strategy", "auth",
        )
        authTimer.count() shouldBe 2L

        val businessTimer = meterRegistry.timer(
            "rate_limit.token.consumption.time",
            "strategy", "business",
        )
        businessTimer.count() shouldBe 1L
    }
}
