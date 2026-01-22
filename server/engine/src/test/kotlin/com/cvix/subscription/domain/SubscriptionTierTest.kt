package com.cvix.subscription.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for SubscriptionTier enum.
 *
 * Tests cover:
 * - Rate limit capacities for each tier
 * - Premium template access
 * - Priority features
 * - Custom feature flags
 * - Tier comparison and ranking
 */
class SubscriptionTierTest {

    @Test
    fun `FREE tier should have correct configuration`() {
        // Given
        val tier = SubscriptionTier.FREE

        // Then
        tier.getDisplayName() shouldBe "Free"
        tier.getRateLimitCapacity() shouldBe 20L
        tier.canAccessPremiumTemplates() shouldBe false
        tier.getMaxPremiumTemplates() shouldBe 0
        tier.hasPriority() shouldBe false
    }

    @Test
    fun `BASIC tier should have correct configuration`() {
        // Given
        val tier = SubscriptionTier.BASIC

        // Then
        tier.getDisplayName() shouldBe "Basic"
        tier.getRateLimitCapacity() shouldBe 40L
        tier.canAccessPremiumTemplates() shouldBe true
        tier.getMaxPremiumTemplates() shouldBe 5
        tier.hasPriority() shouldBe false
    }

    @Test
    fun `PROFESSIONAL tier should have correct configuration`() {
        // Given
        val tier = SubscriptionTier.PROFESSIONAL

        // Then
        tier.getDisplayName() shouldBe "Professional"
        tier.getRateLimitCapacity() shouldBe 100L
        tier.canAccessPremiumTemplates() shouldBe true
        tier.getMaxPremiumTemplates() shouldBe 100
        tier.hasPriority() shouldBe true
    }

    @Test
    fun `FREE tier should not have custom features by default`() {
        // Given
        val tier = SubscriptionTier.FREE

        // When
        val hasFeature = tier.hasFeature("pdf-export")

        // Then
        hasFeature shouldBe false
    }

    @Test
    fun `highest tier should return PROFESSIONAL`() {
        // When
        val highest = SubscriptionTier.highestTier()

        // Then
        highest shouldBe SubscriptionTier.PROFESSIONAL
    }

    @Test
    fun `lowest tier should return FREE`() {
        // When
        val lowest = SubscriptionTier.lowestTier()

        // Then
        lowest shouldBe SubscriptionTier.FREE
    }

    @Test
    fun `isAtLeastAs should correctly compare tiers`() {
        // Then - Professional is at least as capable as any tier
        SubscriptionTier.isAtLeastAs(
            SubscriptionTier.PROFESSIONAL,
            SubscriptionTier.FREE,
        ) shouldBe true
        SubscriptionTier.isAtLeastAs(
            SubscriptionTier.PROFESSIONAL,
            SubscriptionTier.BASIC,
        ) shouldBe true
        SubscriptionTier.isAtLeastAs(
            SubscriptionTier.PROFESSIONAL,
            SubscriptionTier.PROFESSIONAL,
        ) shouldBe true

        // Then - Basic is at least as capable as Free
        SubscriptionTier.isAtLeastAs(SubscriptionTier.BASIC, SubscriptionTier.FREE) shouldBe true
        SubscriptionTier.isAtLeastAs(SubscriptionTier.BASIC, SubscriptionTier.BASIC) shouldBe true
        SubscriptionTier.isAtLeastAs(
            SubscriptionTier.BASIC,
            SubscriptionTier.PROFESSIONAL,
        ) shouldBe false

        // Then - Free is only as capable as Free
        SubscriptionTier.isAtLeastAs(SubscriptionTier.FREE, SubscriptionTier.FREE) shouldBe true
        SubscriptionTier.isAtLeastAs(SubscriptionTier.FREE, SubscriptionTier.BASIC) shouldBe false
    }

    @Test
    fun `different tiers should have different rate limit capacities`() {
        // Then
        val free = SubscriptionTier.FREE.getRateLimitCapacity()
        val basic = SubscriptionTier.BASIC.getRateLimitCapacity()
        val pro = SubscriptionTier.PROFESSIONAL.getRateLimitCapacity()

        free shouldBe 20L
        basic shouldBe 40L
        pro shouldBe 100L

        // Ensure ordering
        (free < basic) shouldBe true
        (basic < pro) shouldBe true
    }

    @Test
    fun `tier comparison should work for template access`() {
        // When
        val freeCanAccess = SubscriptionTier.FREE.canAccessPremiumTemplates()
        val basicCanAccess = SubscriptionTier.BASIC.canAccessPremiumTemplates()
        val proCanAccess = SubscriptionTier.PROFESSIONAL.canAccessPremiumTemplates()

        // Then
        freeCanAccess shouldBe false
        basicCanAccess shouldBe true
        proCanAccess shouldBe true
    }

    @Test
    fun `tier max premium templates should be consistent with access rights`() {
        // When
        val freeMax = SubscriptionTier.FREE.getMaxPremiumTemplates()
        val basicMax = SubscriptionTier.BASIC.getMaxPremiumTemplates()
        val proMax = SubscriptionTier.PROFESSIONAL.getMaxPremiumTemplates()

        // Then - If can't access, max should be 0
        if (!SubscriptionTier.FREE.canAccessPremiumTemplates()) {
            freeMax shouldBe 0
        }

        // Then - Ordering should be correct
        (freeMax <= basicMax) shouldBe true
        (basicMax <= proMax) shouldBe true
    }
}
