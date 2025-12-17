package com.cvix.waitlist.domain

import com.cvix.UnitTest
import com.cvix.common.domain.security.HashUtils
import com.cvix.common.domain.vo.email.Email
import com.cvix.waitlist.domain.event.WaitlistEntryCreatedEvent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.UUID
import org.junit.jupiter.api.Test

@UnitTest
internal class WaitlistEntryTest {

    @Test
    fun `should create valid waitlist entry`() {
        // Arrange
        val id = WaitlistEntryId(UUID.randomUUID())
        val email = Email.of("test@example.com")!!
        val source = WaitlistSource.LANDING_HERO
        val language = Language.ENGLISH

        // Act
        val entry = WaitlistEntry.create(
            id = id,
            email = email,
            sourceRaw = "landing-hero",
            sourceNormalized = source,
            language = language,
            ipHash = hashIpAddress("192.168.1.1"),
            metadata = mapOf("userAgent" to "Mozilla/5.0"),
        )

        // Assert
        entry shouldNotBe null
        entry.id shouldBe id
        entry.email shouldBe email
        entry.sourceRaw shouldBe "landing-hero"
        entry.sourceNormalized shouldBe source
        entry.language shouldBe language
        entry.metadata?.get("userAgent") shouldBe "Mozilla/5.0"
    }

    @Test
    fun `should store hashed IP address`() {
        // Arrange
        val id = WaitlistEntryId(UUID.randomUUID())
        val email = Email.of("test@example.com")!!
        val ipAddress = "192.168.1.2"
        val ipHash = hashIpAddress(ipAddress)

        // Act
        val entry = WaitlistEntry.create(
            id = id,
            email = email,
            sourceRaw = "landing-hero",
            sourceNormalized = WaitlistSource.LANDING_HERO,
            language = Language.ENGLISH,
            ipHash = ipHash,
            metadata = emptyMap(),
        )

        // Assert
        entry.ipHash shouldNotBe null
        entry.ipHash shouldNotBe ipAddress
        // SHA-256 hash is always 64 characters (hex)
        entry.ipHash!!.length shouldBe 64
    }

    @Test
    fun `should handle null IP hash`() {
        // Arrange
        val id = WaitlistEntryId(UUID.randomUUID())
        val email = Email.of("test@example.com")!!

        // Act
        val entry = WaitlistEntry.create(
            id = id,
            email = email,
            sourceRaw = "landing-cta",
            sourceNormalized = WaitlistSource.LANDING_CTA,
            language = Language.SPANISH,
            ipHash = null,
            metadata = emptyMap(),
        )

        // Assert
        entry.ipHash shouldBe null
    }

    @Test
    fun `should record domain event`() {
        // Arrange
        val id = WaitlistEntryId(UUID.randomUUID())
        val email = Email.of("test@example.com")!!

        // Act
        val entry = WaitlistEntry.create(
            id = id,
            email = email,
            sourceRaw = "landing-hero",
            sourceNormalized = WaitlistSource.LANDING_HERO,
            language = Language.ENGLISH,
            ipHash = null,
            metadata = emptyMap(),
        )
        val events = entry.pullDomainEvents()

        // Assert
        events.size shouldBe 1
        val event = events.first()
        event.shouldBeInstanceOf<WaitlistEntryCreatedEvent>()
        event.id shouldBe id.id.toString()
        event.email shouldBe email.value
        event.source shouldBe WaitlistSource.LANDING_HERO.value
    }

    @Test
    fun `should create entry with empty metadata`() {
        // Arrange
        val id = WaitlistEntryId(UUID.randomUUID())
        val email = Email.of("test@example.com")!!

        // Act
        val entry = WaitlistEntry.create(
            id = id,
            email = email,
            sourceRaw = "landing-hero",
            sourceNormalized = WaitlistSource.LANDING_HERO,
            language = Language.ENGLISH,
            ipHash = null,
            metadata = emptyMap(),
        )

        // Assert
        entry.metadata?.isEmpty() shouldBe true
    }

    @Test
    fun `should create entry with null metadata`() {
        // Arrange
        val id = WaitlistEntryId(UUID.randomUUID())
        val email = Email.of("test@example.com")!!

        // Act
        val entry = WaitlistEntry.create(
            id = id,
            email = email,
            sourceRaw = "landing-cta",
            sourceNormalized = WaitlistSource.LANDING_CTA,
            language = Language.SPANISH,
            ipHash = null,
            metadata = null,
        )

        // Assert
        entry.metadata shouldBe null
    }

    @Test
    fun `should parse waitlist source from string`() {
        // Act & Assert
        WaitlistSource.fromString("landing-hero") shouldBe WaitlistSource.LANDING_HERO
        WaitlistSource.fromString("landing-cta") shouldBe WaitlistSource.LANDING_CTA
        WaitlistSource.fromString("blog-cta") shouldBe WaitlistSource.BLOG_CTA
    }

    @Test
    fun `should parse language from string`() {
        // Act & Assert
        Language.fromString("en") shouldBe Language.ENGLISH
        Language.fromString("es") shouldBe Language.SPANISH
    }

    @Test
    fun `should throw IllegalArgumentException for unsupported language codes`() {
        // Act & Assert
        val exception = org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            Language.fromString("fr")
        }
        exception.message shouldBe "Invalid language code 'fr'. Supported codes: en, es"
    }

    private companion object {
        private const val TEST_HMAC_SECRET = "test-hmac-secret-for-testing"
    }
    // Use shared utility for hashing, same as WaitlistJoiner
    private fun hashIpAddress(ipAddress: String): String =
        HashUtils.hmacSha256(ipAddress, TEST_HMAC_SECRET)
}
