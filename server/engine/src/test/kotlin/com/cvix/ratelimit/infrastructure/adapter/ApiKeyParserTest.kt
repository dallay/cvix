package com.cvix.ratelimit.infrastructure.adapter

import com.cvix.UnitTest
import com.cvix.ratelimit.infrastructure.config.RateLimitProperties
import com.cvix.subscription.domain.SubscriptionTier
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

@UnitTest
internal class ApiKeyParserTest {

    @Test
    fun `should extract PROFESSIONAL tier when API key starts with professional prefix`() {
        // Arrange
        val properties = RateLimitProperties(
            apiKeyPrefixes = RateLimitProperties.ApiKeyPrefixConfig(
                professional = "PX001-",
                basic = "BX001-",
            ),
        )
        val parser = ApiKeyParser(properties)

        // Act
        val tier = parser.extractTier("PX001-abc123xyz")

        // Assert
        tier shouldBe SubscriptionTier.PROFESSIONAL
    }

    @Test
    fun `should extract BASIC tier when API key starts with basic prefix`() {
        // Arrange
        val properties = RateLimitProperties(
            apiKeyPrefixes = RateLimitProperties.ApiKeyPrefixConfig(
                professional = "PX001-",
                basic = "BX001-",
            ),
        )
        val parser = ApiKeyParser(properties)

        // Act
        val tier = parser.extractTier("BX001-xyz789abc")

        // Assert
        tier shouldBe SubscriptionTier.BASIC
    }

    @Test
    fun `should default to FREE tier when API key has no recognized prefix`() {
        // Arrange
        val properties = RateLimitProperties(
            apiKeyPrefixes = RateLimitProperties.ApiKeyPrefixConfig(
                professional = "PX001-",
                basic = "BX001-",
            ),
        )
        val parser = ApiKeyParser(properties)

        // Act & Assert
        assertAll(
            { parser.extractTier("unknown-key") shouldBe SubscriptionTier.FREE },
            { parser.extractTier("FX001-test") shouldBe SubscriptionTier.FREE },
            { parser.extractTier("random-string") shouldBe SubscriptionTier.FREE },
            { parser.extractTier("") shouldBe SubscriptionTier.FREE },
        )
    }

    @Test
    fun `should extract tier name as lowercase string`() {
        // Arrange
        val properties = RateLimitProperties(
            apiKeyPrefixes = RateLimitProperties.ApiKeyPrefixConfig(
                professional = "PX001-",
                basic = "BX001-",
            ),
        )
        val parser = ApiKeyParser(properties)

        // Act & Assert
        assertAll(
            { parser.extractTierName("PX001-abc123") shouldBe "professional" },
            { parser.extractTierName("BX001-xyz789") shouldBe "basic" },
            { parser.extractTierName("unknown-key") shouldBe "free" },
        )
    }

    @Test
    fun `should support custom prefix configuration`() {
        // Arrange
        val properties = RateLimitProperties(
            apiKeyPrefixes = RateLimitProperties.ApiKeyPrefixConfig(
                professional = "PRO-",
                basic = "STD-",
            ),
        )
        val parser = ApiKeyParser(properties)

        // Act & Assert
        assertAll(
            { parser.extractTier("PRO-custom-key") shouldBe SubscriptionTier.PROFESSIONAL },
            { parser.extractTier("STD-standard-key") shouldBe SubscriptionTier.BASIC },
            { parser.extractTier("PX001-old-format") shouldBe SubscriptionTier.FREE },
        )
    }

    @Test
    fun `should handle edge cases gracefully`() {
        // Arrange
        val properties = RateLimitProperties(
            apiKeyPrefixes = RateLimitProperties.ApiKeyPrefixConfig(
                professional = "PX001-",
                basic = "BX001-",
            ),
        )
        val parser = ApiKeyParser(properties)

        // Act & Assert
        assertAll(
            // Empty string
            { parser.extractTier("") shouldBe SubscriptionTier.FREE },

            // Only prefix (no key content)
            { parser.extractTier("PX001-") shouldBe SubscriptionTier.PROFESSIONAL },
            { parser.extractTier("BX001-") shouldBe SubscriptionTier.BASIC },

            // Case sensitivity (prefixes are case-sensitive)
            { parser.extractTier("px001-lowercase") shouldBe SubscriptionTier.FREE },
            { parser.extractTier("BXOO1-uppercase") shouldBe SubscriptionTier.FREE },

            // Prefix in the middle or end (should not match)
            { parser.extractTier("key-PX001-middle") shouldBe SubscriptionTier.FREE },
            { parser.extractTier("key-ending-PX001-") shouldBe SubscriptionTier.FREE },
        )
    }

    @Test
    fun `should prioritize professional prefix over basic when both could match`() {
        // Arrange
        val properties = RateLimitProperties(
            apiKeyPrefixes = RateLimitProperties.ApiKeyPrefixConfig(
                professional = "P",
                basic = "PX",
            ),
        )
        val parser = ApiKeyParser(properties)

        // Act
        val tier = parser.extractTier("PX001-test")

        // Assert - Professional is checked first
        tier shouldBe SubscriptionTier.PROFESSIONAL
    }

    @Test
    fun `should use default prefix values when not configured`() {
        // Arrange
        val properties = RateLimitProperties() // Uses defaults
        val parser = ApiKeyParser(properties)

        // Act & Assert
        assertAll(
            { parser.extractTier("PX001-default-prof") shouldBe SubscriptionTier.PROFESSIONAL },
            { parser.extractTier("BX001-default-basic") shouldBe SubscriptionTier.BASIC },
            { parser.extractTier("FX001-no-match") shouldBe SubscriptionTier.FREE },
        )
    }
}
