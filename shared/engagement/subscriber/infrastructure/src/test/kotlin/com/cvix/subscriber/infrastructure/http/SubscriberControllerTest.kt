package com.cvix.subscriber.infrastructure.http

import com.cvix.ControllerTest
import com.cvix.form.application.find.SubscriberFormFinder
import com.cvix.subscriber.application.create.CreateSubscriberCommand
import com.cvix.subscriber.infrastructure.http.request.SubscriberRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.util.*
import net.datafaker.Faker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class SubscriberControllerTest : ControllerTest() {

    private val faker = Faker()
    private lateinit var controller: SubscriberController
    private val formFinder: SubscriberFormFinder = mockk()
    override lateinit var webTestClient: WebTestClient

    @BeforeEach
    override fun setUp() {
        super.setUp()
        every {
            messageSource.getMessage("subscriber.subscribe.success", null, Locale.ENGLISH)
        } returns "Subscription successful!"

        controller = SubscriberController(mediator, messageSource, formFinder)
        webTestClient = buildWebTestClient(controller)
    }

    @Test
    fun `should subscribe successfully`() {
        // Arrange
        val email = faker.internet().emailAddress()
        val request = SubscriberRequest(
            email = email,
            source = "landing-hero",
            language = "en",
        )

        val commandSlot = slot<CreateSubscriberCommand>()
        coEvery { mediator.send(capture(commandSlot)) } returns Unit

        // Act & Assert
        webTestClient.post()
            .uri("/api/subscribers")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept", "application/vnd.api.v1+json")
            .header("X-Workspace-Id", UUID.randomUUID().toString())
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.message").isEqualTo("Subscription successful!")

        coVerify(exactly = 1) { mediator.send(any<CreateSubscriberCommand>()) }
        assertEquals(email, commandSlot.captured.email)
        assertEquals("landing-hero", commandSlot.captured.source)
        assertEquals("en", commandSlot.captured.language)
    }

    @Test
    fun `should reject invalid request`() {
        // Arrange
        val request = SubscriberRequest(
            email = "invalid-email",
            source = "INVALID SOURCE",
            language = "fr",
        )

        // Act & Assert
        webTestClient.post()
            .uri("/api/subscribers")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept", "application/vnd.api.v1+json")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
    }
}
