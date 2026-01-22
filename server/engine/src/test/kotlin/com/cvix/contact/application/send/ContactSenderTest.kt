package com.cvix.contact.application.send

import com.cvix.UnitTest
import com.cvix.contact.domain.CaptchaValidationException
import com.cvix.contact.domain.CaptchaValidator
import com.cvix.contact.domain.ContactData
import com.cvix.contact.domain.ContactNotificationException
import com.cvix.contact.domain.ContactNotifier
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for ContactSender service.
 *
 * Tests the business logic for processing contact form submissions,
 * including CAPTCHA validation and notification delivery.
 */
@UnitTest
class ContactSenderTest {

    private lateinit var captchaValidator: CaptchaValidator
    private lateinit var contactNotifier: ContactNotifier
    private lateinit var contactSender: ContactSender

    @BeforeEach
    fun setUp() {
        captchaValidator = mockk()
        contactNotifier = mockk()
        contactSender = ContactSender(captchaValidator, contactNotifier)
    }

    @Test
    fun `should successfully send contact form when captcha is valid`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        val name = "John Doe"
        val email = "john@example.com"
        val subject = "Test Subject"
        val message = "Test message"
        val hcaptchaToken = "10000000-aaaa-bbbb-cccc-000000000001"
        val ipAddress = "127.0.0.1"

        coEvery { captchaValidator.verify(hcaptchaToken, ipAddress) } returns true
        coEvery { contactNotifier.notify(any<ContactData>()) } returns Unit

        // Act
        contactSender.send(id, name, email, subject, message, hcaptchaToken, ipAddress)

        // Assert
        coVerify(exactly = 1) { captchaValidator.verify(hcaptchaToken, ipAddress) }
        coVerify(exactly = 1) { contactNotifier.notify(any<ContactData>()) }
    }

    @Test
    fun `should throw CaptchaValidationException when captcha validation fails`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        val ipAddress = "127.0.0.1"
        val hcaptchaToken = "invalid-token"

        coEvery { captchaValidator.verify(hcaptchaToken, ipAddress) } returns false

        // Act & Assert
        shouldThrow<CaptchaValidationException> {
            contactSender.send(
                id, "Name", "email@test.com", "Subject", "Message", hcaptchaToken, ipAddress,
            )
        }

        // Verify that notifier was never called
        coVerify(exactly = 1) { captchaValidator.verify(hcaptchaToken, ipAddress) }
        coVerify(exactly = 0) { contactNotifier.notify(any()) }
    }

    @Test
    fun `should propagate notification exceptions to caller`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        val hcaptchaToken = "valid-token"
        val ipAddress = "127.0.0.1"
        val notificationError = RuntimeException("Webhook unavailable")

        coEvery { captchaValidator.verify(any(), any()) } returns true
        coEvery { contactNotifier.notify(any()) } throws notificationError

        // Act & Assert
        val exception = shouldThrow<ContactNotificationException> {
            contactSender.send(
                id, "Name", "email@test.com", "Subject", "Message", hcaptchaToken, ipAddress,
            )
        }

        // The service wraps the original exception with "Failed to send contact notification"
        exception.message shouldBe "Failed to send contact notification"
        exception.cause?.message shouldBe "Webhook unavailable"

        // Verify both services were called
        coVerify(exactly = 1) { captchaValidator.verify(hcaptchaToken, ipAddress) }
        coVerify(exactly = 1) { contactNotifier.notify(any()) }
    }

    @Test
    fun `should handle contact data with special characters`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        val name = "José María"
        val email = "jose@example.com"
        val subject = "Consulta sobre producto #123"
        val message = "Mensaje con ñ, emojis 🚀, y caracteres especiales: <>&\"'"
        val hcaptchaToken = "valid-token"
        val ipAddress = "127.0.0.1"

        coEvery { captchaValidator.verify(any(), any()) } returns true
        coEvery { contactNotifier.notify(any()) } returns Unit

        // Act
        contactSender.send(id, name, email, subject, message, hcaptchaToken, ipAddress)

        // Assert
        coVerify(exactly = 1) {
            contactNotifier.notify(
                match {
                    it.name == "José María" &&
                        it.message.contains("🚀") &&
                        it.message.contains("<>&\"'")
                },
            )
        }
    }

    @Test
    fun `should handle long messages correctly`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        val longMessage = "a".repeat(5000)
        val hcaptchaToken = "valid-token"
        val ipAddress = "127.0.0.1"

        coEvery { captchaValidator.verify(any(), any()) } returns true
        coEvery { contactNotifier.notify(any()) } returns Unit

        // Act
        contactSender.send(
            id, "Test User", "test@example.com", "Long message test",
            longMessage, hcaptchaToken, ipAddress,
        )

        // Assert
        coVerify(exactly = 1) {
            contactNotifier.notify(
                match { it.message.length == 5000 },
            )
        }
    }
}
