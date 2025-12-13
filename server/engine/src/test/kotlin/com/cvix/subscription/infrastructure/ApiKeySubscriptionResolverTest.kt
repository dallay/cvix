package com.cvix.subscription.infrastructure

import com.cvix.UnitTest
import com.cvix.subscription.domain.SubscriptionTier
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * Unit tests for ApiKeySubscriptionResolver.
 *
 * Tests cover:
 * - API key prefix resolution
 * - Fallback to FREE tier for unknown keys
 * - Empty key handling
 */
@UnitTest
class ApiKeySubscriptionResolverTest {

    private val resolver = ApiKeySubscriptionResolver()

    @Test
    fun `should resolve PROFESSIONAL tier from PX001 prefix`() {
        // Given
        val apiKey = "PX001-ABC123XYZ"

        // When
        val tier = runBlocking { resolver.resolve(apiKey) }

        // Then
        tier shouldBe SubscriptionTier.PROFESSIONAL
    }

    @Test
    fun `should resolve BASIC tier from BX001 prefix`() {
        // Given
        val apiKey = "BX001-ABC123XYZ"

        // When
        val tier = runBlocking { resolver.resolve(apiKey) }

        // Then
        tier shouldBe SubscriptionTier.BASIC
    }

    @Test
    fun `should resolve FREE tier for unknown prefix`() {
        // Given
        val apiKey = "UNKNOWN-ABC123XYZ"

        // When
        val tier = runBlocking { resolver.resolve(apiKey) }

        // Then
        tier shouldBe SubscriptionTier.FREE
    }

    @Test
    fun `should resolve FREE tier for empty key`() {
        // Given
        val apiKey = ""

        // When
        val tier = runBlocking { resolver.resolve(apiKey) }

        // Then
        tier shouldBe SubscriptionTier.FREE
    }

    @Test
    fun `should resolve FREE tier for whitespace key`() {
        // Given
        val apiKey = "   "

        // When
        val tier = runBlocking { resolver.resolve(apiKey) }

        // Then
        tier shouldBe SubscriptionTier.FREE
    }

    @Test
    fun `should handle key case sensitivity - prefix must be uppercase`() {
        // Given
        val apiKey = "px001-ABC123XYZ" // lowercase prefix

        // When
        val tier = runBlocking { resolver.resolve(apiKey) }

        // Then
        tier shouldBe SubscriptionTier.FREE // lowercase prefix is not recognized
    }

    @Test
    fun `should resolve multiple PX001 keys`() {
        // Given
        val apiKey1 = "PX001-KEY001"
        val apiKey2 = "PX001-KEY002"

        // When
        val tier1 = runBlocking { resolver.resolve(apiKey1) }
        val tier2 = runBlocking { resolver.resolve(apiKey2) }

        // Then
        tier1 shouldBe SubscriptionTier.PROFESSIONAL
        tier2 shouldBe SubscriptionTier.PROFESSIONAL
    }

    @Test
    fun `should resolve multiple BX001 keys`() {
        // Given
        val apiKey1 = "BX001-KEY001"
        val apiKey2 = "BX001-KEY002"

        // When
        val tier1 = runBlocking { resolver.resolve(apiKey1) }
        val tier2 = runBlocking { resolver.resolve(apiKey2) }

        // Then
        tier1 shouldBe SubscriptionTier.BASIC
        tier2 shouldBe SubscriptionTier.BASIC
    }

    @Test
    fun `should handle long API keys`() {
        // Given
        val apiKey = "PX001-" + "A".repeat(100)

        // When
        val tier = runBlocking { resolver.resolve(apiKey) }

        // Then
        tier shouldBe SubscriptionTier.PROFESSIONAL
    }

    @Test
    fun `should handle API key with special characters in suffix`() {
        // Given
        val apiKey = "PX001-ABC_123-XYZ.789"

        // When
        val tier = runBlocking { resolver.resolve(apiKey) }

        // Then
        tier shouldBe SubscriptionTier.PROFESSIONAL
    }
}
