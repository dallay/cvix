package com.cvix.waitlist.infrastructure.metrics

import com.cvix.UnitTest
import com.cvix.waitlist.domain.WaitlistSource
import io.kotest.matchers.shouldBe
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for WaitlistMetrics.
 *
 * Tests cover:
 * - Recording waitlist joins with both raw and normalized sources
 * - Tracking raw source distribution
 * - Tracking normalized source distribution
 * - Detecting unknown sources for potential new channels
 * - Recording source normalizations
 */
@UnitTest
class WaitlistMetricsTest {

    private lateinit var meterRegistry: MeterRegistry
    private lateinit var metrics: WaitlistMetrics

    @BeforeEach
    fun setUp() {
        meterRegistry = SimpleMeterRegistry()
        metrics = WaitlistMetrics(meterRegistry)
    }

    @Test
    fun `should record waitlist join with raw and normalized sources`() {
        // Given
        val sourceRaw = "landing-hero"
        val sourceNormalized = WaitlistSource.LANDING_HERO

        // When
        metrics.recordWaitlistJoin(sourceRaw, sourceNormalized)

        // Then
        val counter = meterRegistry.counter(
            "waitlist.join.total",
            "source_raw", sourceRaw,
            "source_normalized", sourceNormalized.value,
        )
        counter.count() shouldBe 1.0
    }

    @Test
    fun `should track raw source distribution`() {
        // Given
        val sourceRaw = "twitter-campaign"
        val sourceNormalized = WaitlistSource.UNKNOWN

        // When
        metrics.recordWaitlistJoin(sourceRaw, sourceNormalized)

        // Then
        val counter = meterRegistry.counter(
            "waitlist.source.raw",
            "source", sourceRaw,
        )
        counter.count() shouldBe 1.0
    }

    @Test
    fun `should track normalized source distribution`() {
        // Given
        val sourceRaw = "Landing-Hero"
        val sourceNormalized = WaitlistSource.LANDING_HERO

        // When
        metrics.recordWaitlistJoin(sourceRaw, sourceNormalized)

        // Then
        val counter = meterRegistry.counter(
            "waitlist.source.normalized",
            "source", sourceNormalized.value,
        )
        counter.count() shouldBe 1.0
    }

    @Test
    fun `should track unknown sources separately`() {
        // Given
        val sourceRaw = "producthunt-launch"
        val sourceNormalized = WaitlistSource.UNKNOWN

        // When
        metrics.recordWaitlistJoin(sourceRaw, sourceNormalized)

        // Then - should increment the unknown sources counter
        val unknownCounter = meterRegistry.counter(
            "waitlist.source.unknown",
            "raw_source", sourceRaw,
        )
        unknownCounter.count() shouldBe 1.0
    }

    @Test
    fun `should not track known sources as unknown`() {
        // Given
        val sourceRaw = "landing-hero"
        val sourceNormalized = WaitlistSource.LANDING_HERO

        // When
        metrics.recordWaitlistJoin(sourceRaw, sourceNormalized)

        // Then - should NOT create an unknown sources counter
        val unknownCounter = meterRegistry.find("waitlist.source.unknown")
            .tag("raw_source", sourceRaw)
            .counter()

        unknownCounter shouldBe null
    }

    @Test
    fun `should record source normalization when raw differs from normalized`() {
        // Given
        val sourceRaw = "Landing-CTA"
        val sourceNormalized = WaitlistSource.LANDING_CTA

        // When
        metrics.recordSourceNormalization(sourceRaw, sourceNormalized)

        // Then
        val counter = meterRegistry.counter(
            "waitlist.source.normalization",
            "raw", sourceRaw,
            "normalized", sourceNormalized.value,
        )
        counter.count() shouldBe 1.0
    }

    @Test
    fun `should not record normalization when raw matches normalized`() {
        // Given
        val sourceRaw = "landing-cta"
        val sourceNormalized = WaitlistSource.LANDING_CTA

        // When
        metrics.recordSourceNormalization(sourceRaw, sourceNormalized)

        // Then - should NOT create a normalization counter
        val counter = meterRegistry.find("waitlist.source.normalization")
            .tag("raw", sourceRaw)
            .counter()

        counter shouldBe null
    }

    @Test
    fun `should track multiple joins from different sources`() {
        // Given & When
        metrics.recordWaitlistJoin("landing-hero", WaitlistSource.LANDING_HERO)
        metrics.recordWaitlistJoin("landing-cta", WaitlistSource.LANDING_CTA)
        metrics.recordWaitlistJoin("twitter-campaign", WaitlistSource.UNKNOWN)
        metrics.recordWaitlistJoin("reddit-post", WaitlistSource.UNKNOWN)

        // Then - check total join counter
        val totalJoins = meterRegistry.find("waitlist.join.total").counters().sumOf { it.count() }
        totalJoins shouldBe 4.0

        // Check unknown sources counter
        val unknownSources = meterRegistry.find("waitlist.source.unknown").counters().size
        unknownSources shouldBe 2 // twitter-campaign and reddit-post
    }

    @Test
    fun `should sanitize excessively long source values to prevent cardinality explosion`() {
        // Given - a source value longer than 100 characters
        val longSource = "a".repeat(150)
        val sourceNormalized = WaitlistSource.UNKNOWN

        // When
        metrics.recordWaitlistJoin(longSource, sourceNormalized)

        // Then - should be sanitized to "invalid"
        val counter = meterRegistry.counter(
            "waitlist.source.raw",
            "source", "invalid",
        )
        counter.count() shouldBe 1.0
    }

    @Test
    fun `should sanitize source values with invalid characters to prevent cardinality explosion`() {
        // Given - sources with special characters, spaces, or other invalid chars
        val invalidSources = listOf(
            "landing<script>alert('xss')</script>",
            "source with spaces",
            "source@with!special#chars",
            "source/with/slashes",
        )

        // When
        invalidSources.forEach { source ->
            metrics.recordWaitlistJoin(source, WaitlistSource.UNKNOWN)
        }

        // Then - all should be sanitized to "invalid"
        val counter = meterRegistry.counter(
            "waitlist.source.raw",
            "source", "invalid",
        )
        counter.count() shouldBe 4.0
    }

    @Test
    fun `should allow valid source values through sanitization`() {
        // Given - valid sources with alphanumeric, dash, and underscore
        val validSources = listOf(
            "landing-hero",
            "landing_cta",
            "twitter123",
            "reddit-post-2024",
        )

        // When & Then
        validSources.forEach { source ->
            metrics.recordWaitlistJoin(source, WaitlistSource.UNKNOWN)
            val counter = meterRegistry.counter(
                "waitlist.source.raw",
                "source", source,
            )
            counter.count() shouldBe 1.0
        }
    }

    @Test
    fun `should sanitize invalid sources in normalization recording`() {
        // Given - an invalid source
        val invalidSource = "source with spaces"
        val sourceNormalized = WaitlistSource.UNKNOWN

        // When
        metrics.recordSourceNormalization(invalidSource, sourceNormalized)

        // Then - should be sanitized to "invalid"
        val counter = meterRegistry.counter(
            "waitlist.source.normalization",
            "raw", "invalid",
            "normalized", sourceNormalized.value,
        )
        counter.count() shouldBe 1.0
    }
}
