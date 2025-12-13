package com.cvix.resume.domain

import com.cvix.subscription.domain.SubscriptionTier
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for TemplateMetadata subscription tier access control.
 *
 * Tests cover:
 * - Template accessibility based on user tier
 * - Premium template restrictions
 * - Tier comparison and filtering
 */
class TemplateMetadataTest {

    @Test
    fun `free template should be accessible by all tiers`() {
        // Given
        val template = TemplateMetadata(
            id = "template-free",
            name = "Free Template",
            version = "1.0",
            templatePath = "templates/free",
            requiredSubscriptionTier = SubscriptionTier.FREE,
        )

        // Then - all tiers can access free templates
        template.isAccessibleBy(SubscriptionTier.FREE) shouldBe true
        template.isAccessibleBy(SubscriptionTier.BASIC) shouldBe true
        template.isAccessibleBy(SubscriptionTier.PROFESSIONAL) shouldBe true
    }

    @Test
    fun `basic premium template should not be accessible by free users`() {
        // Given
        val template = TemplateMetadata(
            id = "template-premium-basic",
            name = "Premium Basic Template",
            version = "1.0",
            templatePath = "templates/premium/basic",
            requiredSubscriptionTier = SubscriptionTier.BASIC,
        )

        // Then
        template.isAccessibleBy(SubscriptionTier.FREE) shouldBe false
        template.isAccessibleBy(SubscriptionTier.BASIC) shouldBe true
        template.isAccessibleBy(SubscriptionTier.PROFESSIONAL) shouldBe true
    }

    @Test
    fun `professional template should only be accessible by professional users`() {
        // Given
        val template = TemplateMetadata(
            id = "template-professional",
            name = "Professional Template",
            version = "1.0",
            templatePath = "templates/professional",
            requiredSubscriptionTier = SubscriptionTier.PROFESSIONAL,
        )

        // Then
        template.isAccessibleBy(SubscriptionTier.FREE) shouldBe false
        template.isAccessibleBy(SubscriptionTier.BASIC) shouldBe false
        template.isAccessibleBy(SubscriptionTier.PROFESSIONAL) shouldBe true
    }

    @Test
    fun `template should default to free tier if not specified`() {
        // Given
        val template = TemplateMetadata(
            id = "template-default",
            name = "Default Template",
            version = "1.0",
            templatePath = "templates/default",
            // requiredSubscriptionTier not specified, should default to FREE
        )

        // Then - should be accessible by all
        template.isAccessibleBy(SubscriptionTier.FREE) shouldBe true
        template.isAccessibleBy(SubscriptionTier.BASIC) shouldBe true
        template.isAccessibleBy(SubscriptionTier.PROFESSIONAL) shouldBe true
    }

    @Test
    fun `template accessibility should be transitive through tiers`() {
        // Given
        val templates = listOf(
            TemplateMetadata(
                "t1",
                "Free",
                "1.0",
                templatePath = "path1",
                requiredSubscriptionTier = SubscriptionTier.FREE,
            ),
            TemplateMetadata(
                "t2",
                "Basic",
                "1.0",
                templatePath = "path2",
                requiredSubscriptionTier = SubscriptionTier.BASIC,
            ),
            TemplateMetadata(
                "t3",
                "Pro",
                "1.0",
                templatePath = "path3",
                requiredSubscriptionTier = SubscriptionTier.PROFESSIONAL,
            ),
        )

        // When - filter for FREE user
        val freeUserTemplates = templates.filter { it.isAccessibleBy(SubscriptionTier.FREE) }

        // Then
        freeUserTemplates.map { it.id } shouldBe listOf("t1")

        // When - filter for BASIC user
        val basicUserTemplates = templates.filter { it.isAccessibleBy(SubscriptionTier.BASIC) }

        // Then
        basicUserTemplates.map { it.id } shouldBe listOf("t1", "t2")

        // When - filter for PROFESSIONAL user
        val proUserTemplates = templates.filter { it.isAccessibleBy(SubscriptionTier.PROFESSIONAL) }

        // Then
        proUserTemplates.map { it.id } shouldBe listOf("t1", "t2", "t3")
    }

    @Test
    fun `template with full metadata should maintain accessibility`() {
        // Given
        val template = TemplateMetadata(
            id = "template-full",
            name = "Full Template",
            version = "1.0",
            descriptions = mapOf(Locale.EN to "English description"),
            supportedLocales = listOf(Locale.EN),
            templatePath = "templates/full",
            previewUrl = "https://example.com/preview.png",
            params = TemplateParams(colorPalette = "modern"),
            requiredSubscriptionTier = SubscriptionTier.BASIC,
        )

        // Then
        template.isAccessibleBy(SubscriptionTier.FREE) shouldBe false
        template.isAccessibleBy(SubscriptionTier.BASIC) shouldBe true
        template.isAccessibleBy(SubscriptionTier.PROFESSIONAL) shouldBe true
    }
}
