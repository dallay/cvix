package com.cvix.contact.domain

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for ContactData domain model.
 *
 * Tests validation rules and business invariants for contact form data.
 */
class ContactDataTest {

    @Test
    fun `should create valid ContactData with all required fields`() {
        // Arrange & Act
        val contactData = ContactData(
            name = "John Doe",
            email = "john.doe@example.com",
            subject = "Test Subject",
            message = "This is a test message",
        )

        // Assert
        contactData shouldNotBe null
        contactData.name shouldBe "John Doe"
        contactData.email shouldBe "john.doe@example.com"
        contactData.subject shouldBe "Test Subject"
        contactData.message shouldBe "This is a test message"
    }

    @Test
    fun `should accept name with special characters`() {
        // Arrange & Act
        val contactData = ContactData(
            name = "José María O'Brien-García",
            email = "jose@example.com",
            subject = "Test",
            message = "Message",
        )

        // Assert
        contactData.name shouldBe "José María O'Brien-García"
    }

    @Test
    fun `should accept valid email formats`() {
        // Arrange
        val validEmails = listOf(
            "simple@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_name@example-domain.com",
        )

        // Act & Assert
        validEmails.forEach { email ->
            val contactData = ContactData(
                name = "Test User",
                email = email,
                subject = "Test",
                message = "Message",
            )
            contactData.email shouldBe email
        }
    }

    @Test
    fun `should accept message with maximum length`() {
        // Arrange
        val longMessage = "a".repeat(5000)

        // Act
        val contactData = ContactData(
            name = "Test User",
            email = "test@example.com",
            subject = "Test",
            message = longMessage,
        )

        // Assert
        contactData.message shouldBe longMessage
        contactData.message.length shouldBe 5000
    }

    @Test
    fun `should accept subject with special characters`() {
        // Arrange & Act
        val contactData = ContactData(
            name = "Test User",
            email = "test@example.com",
            subject = "Regarding: Product #123 - Special Offer! (50% OFF)",
            message = "Message",
        )

        // Assert
        contactData.subject shouldBe "Regarding: Product #123 - Special Offer! (50% OFF)"
    }

    @Test
    fun `should accept unicode characters in all text fields`() {
        // Arrange & Act
        val contactData = ContactData(
            name = "王小明",
            email = "user@example.com",
            subject = "Consulta sobre 产品",
            message = "Mensaje con ñ, á, é, í, ó, ú y emojis 🚀✨",
        )

        // Assert
        contactData.name shouldBe "王小明"
        contactData.subject shouldBe "Consulta sobre 产品"
        contactData.message shouldBe "Mensaje con ñ, á, é, í, ó, ú y emojis 🚀✨"
    }

    @Test
    fun `should handle whitespace in text fields`() {
        // Arrange & Act
        val contactData = ContactData(
            name = "  John Doe  ",
            email = "john@example.com",
            subject = "  Test Subject  ",
            message = "  Message with spaces  ",
        )

        // Assert - Note: We don't trim in the domain model
        contactData.name shouldBe "  John Doe  "
        contactData.subject shouldBe "  Test Subject  "
        contactData.message shouldBe "  Message with spaces  "
    }
}
