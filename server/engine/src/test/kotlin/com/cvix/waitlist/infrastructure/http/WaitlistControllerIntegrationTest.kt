package com.cvix.waitlist.infrastructure.http

import com.cvix.ControllerIntegrationTest
import com.cvix.waitlist.infrastructure.http.request.JoinWaitlistRequest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf

internal class WaitlistControllerIntegrationTest : ControllerIntegrationTest() {
    @Test
    fun `should join waitlist successfully`() {
        // Arrange
        val email = "jane.doe@test.com"
        val request = JoinWaitlistRequest(
            email = email,
            source = "landing-hero",
            language = "en",
        )
        webTestClient.mutateWith(csrf()).post()
            .uri("/api/waitlist")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.message").isEqualTo("You've been added to the waitlist!")
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
        webTestClient.mutateWith(csrf()).post()
            .uri("/api/waitlist")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.type").exists()
            .jsonPath("$.title").isEqualTo("Validation Failed")
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.detail").isEqualTo("Request validation failed. Please check the provided data.")
            .jsonPath("$.instance").isEqualTo("/api/waitlist")
            .jsonPath("$.errors").isArray
            .jsonPath("$.errors[0].field").isEqualTo("email")
    }

    @Test
    fun `should not join an existing email`() {
        // Arrange
        val email = "jack.ghost@test.com"
        val request = JoinWaitlistRequest(
            email = email,
            source = "landing-hero",
            language = "en",
        )
        // First join attempt
        webTestClient.mutateWith(csrf()).post()
            .uri("/api/waitlist")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.message").isEqualTo("You've been added to the waitlist!")
        // Second join attempt with the same email
        webTestClient.mutateWith(csrf()).post()
            .uri("/api/waitlist")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .jsonPath("$.error").isEqualTo("EMAIL_ALREADY_EXISTS")
            .jsonPath("$.message").isEqualTo("This email is already on the waitlist")
    }
}
