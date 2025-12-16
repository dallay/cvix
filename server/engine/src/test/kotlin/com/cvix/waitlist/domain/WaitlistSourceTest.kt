package com.cvix.waitlist.domain

import com.cvix.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for WaitlistSource enum.
 *
 * Tests cover:
 * - Normalization of known source values
 * - Case-insensitive parsing
 * - Handling of unknown sources (normalized to UNKNOWN)
 * - Extensibility for future growth
 */
@UnitTest
class WaitlistSourceTest {

    @Test
    fun `should return correct enum for known source values`() {
        // Given & When & Then
        WaitlistSource.fromString("landing-hero") shouldBe WaitlistSource.LANDING_HERO
        WaitlistSource.fromString("landing-cta") shouldBe WaitlistSource.LANDING_CTA
        WaitlistSource.fromString("blog-cta") shouldBe WaitlistSource.BLOG_CTA
        WaitlistSource.fromString("unknown") shouldBe WaitlistSource.UNKNOWN
    }

    @Test
    fun `should be case-insensitive when parsing source`() {
        // Given & When & Then
        WaitlistSource.fromString("LANDING-HERO") shouldBe WaitlistSource.LANDING_HERO
        WaitlistSource.fromString("Landing-Hero") shouldBe WaitlistSource.LANDING_HERO
        WaitlistSource.fromString("LANDING-CTA") shouldBe WaitlistSource.LANDING_CTA
        WaitlistSource.fromString("Blog-CTA") shouldBe WaitlistSource.BLOG_CTA
    }

    @Test
    fun `should normalize unknown source to UNKNOWN`() {
        // Given & When & Then
        WaitlistSource.fromString("twitter-campaign") shouldBe WaitlistSource.UNKNOWN
        WaitlistSource.fromString("reddit-post") shouldBe WaitlistSource.UNKNOWN
        WaitlistSource.fromString("email-newsletter") shouldBe WaitlistSource.UNKNOWN
        WaitlistSource.fromString("partner-referral") shouldBe WaitlistSource.UNKNOWN
    }

    @Test
    fun `should handle empty and whitespace source as unknown`() {
        // Given & When & Then
        WaitlistSource.fromString("") shouldBe WaitlistSource.UNKNOWN
        WaitlistSource.fromString("   ") shouldBe WaitlistSource.UNKNOWN
    }

    @Test
    fun `should return correct value property for each enum`() {
        // Given & When & Then
        WaitlistSource.LANDING_HERO.value shouldBe "landing-hero"
        WaitlistSource.LANDING_CTA.value shouldBe "landing-cta"
        WaitlistSource.BLOG_CTA.value shouldBe "blog-cta"
        WaitlistSource.UNKNOWN.value shouldBe "unknown"
    }

    @Test
    fun `should support extensibility for future sources without code changes`() {
        // Given - simulating a new marketing channel that doesn't exist yet
        val futureSources = listOf(
            "youtube-video",
            "podcast-mention",
            "hackernews-post",
            "producthunt-launch",
            "conference-talk",
        )

        // When & Then - all future sources should be normalized to UNKNOWN
        futureSources.forEach { source ->
            WaitlistSource.fromString(source) shouldBe WaitlistSource.UNKNOWN
        }
    }
}
