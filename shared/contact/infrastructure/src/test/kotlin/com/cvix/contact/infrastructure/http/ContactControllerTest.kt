package com.cvix.contact.infrastructure.http

import com.cvix.ControllerTest
import com.cvix.contact.application.send.SendContactCommand
import com.cvix.contact.domain.CaptchaValidationException
import com.cvix.contact.infrastructure.http.request.SendContactRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.slot
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Integration tests for ContactController.
 *
 * Tests HTTP layer including request validation, headers, status codes,
 * and error handling.
 */
internal class ContactControllerTest : ControllerTest() {

    private lateinit var controller: ContactController
    override lateinit var webTestClient: WebTestClient

    @BeforeEach
    override fun setUp() {
        super.setUp()

        // Mock English messages
        every {
            messageSource.getMessage("contact.success", null, Locale.ENGLISH)
        } returns "Message sent successfully"

        // Mock Spanish messages
        every {
            messageSource.getMessage("contact.success", null, Locale.forLanguageTag("es"))
        } returns "Mensaje enviado con éxito"

        every {
            messageSource.getMessage("contact.error", null, Locale.ENGLISH)
        } returns "An error occurred while sending your message. Please try again later."

        // Initialize controller and web client
        controller = ContactController(mediator, messageSource)
        webTestClient = buildWebTestClient(controller)
    }

    @Test
    fun `should return 201 Created on successful contact form submission`() {
        // Arrange
        val request = createValidRequest()
        val commandSlot = slot<SendContactCommand>()
        coEvery { mediator.send(capture(commandSlot)) } returns Unit

        // Act & Assert
        webTestClient
            .post()
            .uri("/api/contact")
            .header("API-Version", "v1")
            .header("Accept-Language", "en")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.message").isEqualTo("Message sent successfully")

        coVerify(exactly = 1) { mediator.send(any<SendContactCommand>()) }

        // Verify command values
        assertEquals("John Doe", commandSlot.captured.name)
        assertEquals("john.doe@example.com", commandSlot.captured.email)
    }

    @Test
    fun `should return Spanish message when Accept-Language is es`() {
        // Arrange
        val request = createValidRequest()
        coEvery { mediator.send(any<SendContactCommand>()) } returns Unit

        // Act & Assert
        webTestClient
            .post()
            .uri("/api/contact")
            .header("API-Version", "v1")
            .header("Accept-Language", "es")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.message").isEqualTo("Mensaje enviado con éxito")

        coVerify(exactly = 1) { mediator.send(any<SendContactCommand>()) }
    }

    @Test
    fun `should return 400 Bad Request when name is missing`() {
        // Arrange
        val invalidRequest = mapOf(
            "email" to "test@example.com",
            "subject" to "Test",
            "message" to "Message",
            "hcaptchaToken" to "token",
            // Missing 'name'
        )

        // Act & Assert
        webTestClient
            .post()
            .uri("/api/contact")
            .header("API-Version", "v1")
            .header("Accept-Language", "en")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isBadRequest

        coVerify(exactly = 0) { mediator.send(any<SendContactCommand>()) }
    }

    @Test
    fun `should return 400 Bad Request when email is invalid`() {
        // Arrange
        val invalidRequest = SendContactRequest(
            name = "Test User",
            email = "invalid-email",
            subject = "Test",
            message = "Message",
            hcaptchaToken = "token",
        )

        // Act & Assert
        webTestClient
            .post()
            .uri("/api/contact")
            .header("API-Version", "v1")
            .header("Accept-Language", "en")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isBadRequest

        coVerify(exactly = 0) { mediator.send(any<SendContactCommand>()) }
    }

    @Test
    fun `should return 400 Bad Request when message is too long`() {
        // Arrange
        val invalidRequest = SendContactRequest(
            name = "Test User",
            email = "test@example.com",
            subject = "Test",
            message = "a".repeat(5001), // Exceeds 5000 char limit
            hcaptchaToken = "token",
        )

        // Act & Assert
        webTestClient
            .post()
            .uri("/api/contact")
            .header("API-Version", "v1")
            .header("Accept-Language", "en")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isBadRequest

        coVerify(exactly = 0) { mediator.send(any<SendContactCommand>()) }
    }

    @Test
    fun `should return 400 Bad Request when captcha token is missing`() {
        // Arrange
        val invalidRequest = mapOf(
            "name" to "Test User",
            "email" to "test@example.com",
            "subject" to "Test",
            "message" to "Message",
            // Missing 'hcaptchaToken'
        )

        // Act & Assert
        webTestClient
            .post()
            .uri("/api/contact")
            .header("API-Version", "v1")
            .header("Accept-Language", "en")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isBadRequest

        coVerify(exactly = 0) { mediator.send(any<SendContactCommand>()) }
    }

    @Test
    fun `should return 400 Bad Request when captcha validation fails`() {
        // Arrange
        val request = createValidRequest()
        coEvery { mediator.send(any<SendContactCommand>()) } throws
            CaptchaValidationException("Invalid CAPTCHA")

        // Act & Assert
        webTestClient
            .post()
            .uri("/api/contact")
            .header("API-Version", "v1")
            .header("Accept-Language", "en")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest

        coVerify(exactly = 1) { mediator.send(any<SendContactCommand>()) }
    }

    @Test
    fun `should return 500 Internal Server Error when notification fails`() {
        // Arrange
        val request = createValidRequest()
        coEvery { mediator.send(any<SendContactCommand>()) } throws
            RuntimeException("Webhook unavailable")

        // Act & Assert
        webTestClient
            .post()
            .uri("/api/contact")
            .header("API-Version", "v1")
            .header("Accept-Language", "en")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().is5xxServerError

        coVerify(exactly = 1) { mediator.send(any<SendContactCommand>()) }
    }

    @Test
    fun `should accept maximum length values for all fields`() {
        // Arrange
        val request = SendContactRequest(
            name = "a".repeat(100),
            email = "test@example.com",
            subject = "s".repeat(200),
            message = "m".repeat(5000),
            hcaptchaToken = "token123",
        )
        coEvery { mediator.send(any<SendContactCommand>()) } returns Unit

        // Act & Assert
        webTestClient
            .post()
            .uri("/api/contact")
            .header("API-Version", "v1")
            .header("Accept-Language", "en")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated

        coVerify(exactly = 1) { mediator.send(any<SendContactCommand>()) }
    }

    @Test
    fun `should handle unicode characters in all fields`() {
        // Arrange
        val request = SendContactRequest(
            name = "José María 王小明",
            email = "test@example.com",
            subject = "Consulta sobre 产品",
            message = "Mensaje con ñ, emojis 🚀✨, y caracteres especiales",
            hcaptchaToken = "token123",
        )
        coEvery { mediator.send(any<SendContactCommand>()) } returns Unit

        // Act & Assert
        webTestClient
            .post()
            .uri("/api/contact")
            .header("API-Version", "v1")
            .header("Accept-Language", "en")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated

        coVerify(exactly = 1) { mediator.send(any<SendContactCommand>()) }
    }

    private fun createValidRequest() = SendContactRequest(
        name = "John Doe",
        email = "john.doe@example.com",
        subject = "Test Subject",
        message = "This is a test message",
        hcaptchaToken = "10000000-aaaa-bbbb-cccc-000000000001",
    )
}
