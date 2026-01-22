package com.cvix.resume.infrastructure.http

import com.cvix.ControllerTest
import com.cvix.authentication.infrastructure.ApplicationSecurityProperties
import com.cvix.resume.ResumeTestFixtures
import com.cvix.resume.application.generate.GenerateResumeCommand
import com.cvix.resume.domain.Locale
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.io.ByteArrayInputStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class ResumeGeneratorControllerTest : ControllerTest() {
    private val applicationSecurityProperties = mockk<ApplicationSecurityProperties> {
        every { contentSecurityPolicy } returns "default-src 'self'"
    }
    private val controller = ResumeGeneratorController(mediator, applicationSecurityProperties)
    override val webTestClient: WebTestClient = buildWebTestClient(controller)
    private val pdfBytes = "PDF content".toByteArray()

    @BeforeEach
    override fun setUp() {
        super.setUp()
        coEvery { mediator.send(any<GenerateResumeCommand>()) } returns ByteArrayInputStream(
            pdfBytes,
        )
    }

    @Test
    fun `should generate resume PDF successfully`() {
        val request = ResumeTestFixtures.createValidResumeRequestContent()

        webTestClient.post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_PDF)
            .expectHeader().exists(HttpHeaders.CONTENT_DISPOSITION)
            .expectHeader().exists("X-Generation-Time-Ms")
            .expectBody()
            .returnResult()

        val commandSlot = slot<GenerateResumeCommand>()
        coVerify(exactly = 1) { mediator.send(capture(commandSlot)) }
        assertEquals(Locale.EN, commandSlot.captured.locale)
    }

    @Test
    fun `should generate resume with Spanish locale from Accept-Language header`() {
        val request = ResumeTestFixtures.createValidResumeRequestContent()

        webTestClient.post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_LANGUAGE, "es-ES")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_PDF)

        val commandSlot = slot<GenerateResumeCommand>()
        coVerify(exactly = 1) { mediator.send(capture(commandSlot)) }
        assertEquals(Locale.ES, commandSlot.captured.locale)
    }

    @Test
    fun `should return 400 for invalid locale`() {
        val request = ResumeTestFixtures.createValidResumeRequestContent()

        webTestClient.post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_LANGUAGE, "invalid-locale")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
    }

    // Note: Payload size validation happens at the controller level
    // and requires the actual payload to exceed 100KB, not just the header

    @Test
    fun `should set security headers in response`() {
        val request = ResumeTestFixtures.createValidResumeRequestContent()

        webTestClient.post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("Content-Security-Policy")
            .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
            .expectHeader().valueEquals("X-Frame-Options", "DENY")
            .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin")
    }
}
