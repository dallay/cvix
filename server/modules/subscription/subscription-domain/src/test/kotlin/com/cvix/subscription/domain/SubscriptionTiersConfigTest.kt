package com.cvix.subscription.domain

import com.cvix.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

@UnitTest
internal class SubscriptionTiersConfigTest {
    @Test
    fun `getConfig returns correct config for each default tier`() {
        val config = SubscriptionTiersConfig()
        val free = config.getConfig(SubscriptionTier.FREE)
        free.tier shouldBe SubscriptionTier.FREE
        free.displayName shouldBe "Free"
        free.rateLimitCapacity shouldBe 20L
        free.premiumTemplatesEnabled shouldBe false
        free.maxPremiumTemplates shouldBe 0
        free.priority shouldBe false

        val basic = config.getConfig(SubscriptionTier.BASIC)
        basic.tier shouldBe SubscriptionTier.BASIC
        basic.displayName shouldBe "Basic"
        basic.rateLimitCapacity shouldBe 40L
        basic.premiumTemplatesEnabled shouldBe true
        basic.maxPremiumTemplates shouldBe 5
        basic.priority shouldBe false

        val pro = config.getConfig(SubscriptionTier.PROFESSIONAL)
        pro.tier shouldBe SubscriptionTier.PROFESSIONAL
        pro.displayName shouldBe "Professional"
        pro.rateLimitCapacity shouldBe 100L
        pro.premiumTemplatesEnabled shouldBe true
        pro.maxPremiumTemplates shouldBe 100
        pro.priority shouldBe true
    }

    @Test
    fun `getConfig throws when FREE config is missing`() {
        val customConfig = SubscriptionTiersConfig(
            tiers = mapOf(
                SubscriptionTier.BASIC to SubscriptionTierConfig(
                    tier = SubscriptionTier.BASIC,
                    displayName = "Basic",
                    rateLimitCapacity = 40L,
                    premiumTemplatesEnabled = true,
                    maxPremiumTemplates = 5,
                    priority = false,
                ),
            ),
        )
        val exception = org.junit.jupiter.api.assertThrows<IllegalStateException> {
            customConfig.getConfig(SubscriptionTier.PROFESSIONAL)
        }
        exception.message shouldBe
            "SubscriptionTiersConfig is missing the FREE tier configuration. This is required as a fallback."
    }

    @Test
    fun `customFeatures are empty by default`() {
        val config = SubscriptionTiersConfig()
        config.getConfig(SubscriptionTier.FREE).customFeatures.isEmpty() shouldBe true
    }

    @Test
    fun `customFeatures are returned when provided`() {
        val features = mapOf("featureX" to true, "featureY" to false)
        val tierConfig = SubscriptionTierConfig(
            tier = SubscriptionTier.BASIC,
            displayName = "Basic",
            rateLimitCapacity = 40L,
            premiumTemplatesEnabled = true,
            maxPremiumTemplates = 5,
            priority = false,
            customFeatures = features,
        )
        val config = SubscriptionTiersConfig(tiers = mapOf(SubscriptionTier.BASIC to tierConfig))
        config.getConfig(SubscriptionTier.BASIC).customFeatures shouldBe features
    }
}
