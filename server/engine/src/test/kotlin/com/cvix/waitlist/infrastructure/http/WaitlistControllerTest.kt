package com.cvix.waitlist.infrastructure.http

import com.cvix.ControllerTest
import com.cvix.waitlist.application.create.JoinWaitlistCommand
import com.cvix.waitlist.domain.EmailAlreadyExistsException
import com.cvix.waitlist.infrastructure.http.request.JoinWaitlistRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import net.datafaker.Faker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

internal class WaitlistControllerTest : ControllerTest() {

    private val controller = WaitlistController(mediator)
    override val webTestClient = buildWebTestClient(controller)
    private val faker = Faker()

    @BeforeEach
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun `should join waitlist successfully`() {
        // Arrange
        val email = faker.internet().emailAddress()
        val request = JoinWaitlistRequest(
            email = email,
            source = "landing-hero",
            language = "en",
        )

        val commandSlot = slot<JoinWaitlistCommand>()
        coEvery { mediator.send(capture(commandSlot)) } returns Unit

        // Act & Assert
        webTestClient.post()
            .uri("/api/waitlist")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.message").isEqualTo("You've been added to the waitlist!")

        coVerify(exactly = 1) { mediator.send(any<JoinWaitlistCommand>()) }
        assertEquals(request.email, commandSlot.captured.email)
        assertEquals(request.source, commandSlot.captured.source)
    }

    @Test
    fun `should reject invalid email`() {
        // Arrange
        val request = JoinWaitlistRequest(
            email = "invalid-email",
            source = "landing-hero",
            language = "en",
        )

        // Act & Assert
        webTestClient.post()
            .uri("/api/waitlist")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should not join an existing email`() {
        // Arrange
        val email = faker.internet().emailAddress()
        val request = JoinWaitlistRequest(
            email = email,
            source = "landing-hero",
            language = "en",
        )

        coEvery { mediator.send(any<JoinWaitlistCommand>()) } throws EmailAlreadyExistsException(email)

        // Act & Assert
        webTestClient.post()
            .uri("/api/waitlist")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isEqualTo(409)

        coVerify(exactly = 1) { mediator.send(any<JoinWaitlistCommand>()) }
    }
}
