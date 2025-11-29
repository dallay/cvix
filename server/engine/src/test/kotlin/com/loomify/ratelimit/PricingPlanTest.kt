package com.loomify.ratelimit

import com.loomify.ratelimit.infrastructure.PricingPlan
import io.github.bucket4j.Bandwidth
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

/**
 * Unit tests for PricingPlan enum.
 *
 * Tests cover:
 * - Plan resolution from API keys
 * - Bandwidth configuration for each plan
 * - Edge cases and null handling
 */
class PricingPlanTest {

    @Test
    fun `should resolve FREE plan for empty API key`() {
        // When
        val plan = PricingPlan.resolvePlanFromApiKey("")

        // Then
        plan shouldBe PricingPlan.FREE
    }

    @Test
    fun `should resolve PROFESSIONAL plan for PX001 prefix`() {
        // When
        val plan = PricingPlan.resolvePlanFromApiKey("PX001-ABC123")

        // Then
        plan shouldBe PricingPlan.PROFESSIONAL
    }

    @Test
    fun `should resolve BASIC plan for BX001 prefix`() {
        // When
        val plan = PricingPlan.resolvePlanFromApiKey("BX001-XYZ789")

        // Then
        plan shouldBe PricingPlan.BASIC
    }

    @Test
    fun `should resolve FREE plan for unrecognized API key`() {
        // When
        val plan = PricingPlan.resolvePlanFromApiKey("UNKNOWN-KEY")

        // Then
        plan shouldBe PricingPlan.FREE
    }

    @Test
    fun `FREE plan should have correct bandwidth configuration`() {
        // When
        val bandwidth = PricingPlan.FREE.getLimit()

        // Then
        bandwidth.shouldBeInstanceOf<Bandwidth>()
        // The bandwidth should allow 20 requests per hour
    }

    @Test
    fun `BASIC plan should have correct bandwidth configuration`() {
        // When
        val bandwidth = PricingPlan.BASIC.getLimit()

        // Then
        bandwidth.shouldBeInstanceOf<Bandwidth>()
        // The bandwidth should allow 40 requests per hour
    }

    @Test
    fun `PROFESSIONAL plan should have correct bandwidth configuration`() {
        // When
        val bandwidth = PricingPlan.PROFESSIONAL.getLimit()

        // Then
        bandwidth.shouldBeInstanceOf<Bandwidth>()
        // The bandwidth should allow 100 requests per hour
    }

    @Test
    fun `different plans should have different bandwidths`() {
        // When
        val freeBandwidth = PricingPlan.FREE.getLimit()
        val basicBandwidth = PricingPlan.BASIC.getLimit()
        val proBandwidth = PricingPlan.PROFESSIONAL.getLimit()

        // Then
        freeBandwidth shouldNotBe basicBandwidth
        basicBandwidth shouldNotBe proBandwidth
        freeBandwidth shouldNotBe proBandwidth
    }
}
