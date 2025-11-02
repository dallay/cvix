package com.loomify.resume.infrastructure.web

import com.loomify.UnitTest
import com.loomify.resume.application.command.GenerateResumeCommand
import com.loomify.resume.application.handler.GenerateResumeCommandHandler
import io.mockk.coEvery
import io.mockk.mockk
import java.io.ByteArrayInputStream
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

/**
 * Unit test for ResumeController.
 * Tests HTTP layer with mocked application handler.
 *
 * Mocking Strategy (per user guidance):
 * - Mock application handler: GenerateResumeCommandHandler
 * - Focus on controller behavior: DTO mapping, HTTP status codes, content types
 */
@UnitTest
class ResumeControllerTest {

    private val handler = mockk<GenerateResumeCommandHandler>()
    private val controller = ResumeController(handler)
    private lateinit var webTestClient: WebTestClient

    private val pdfBytes = "fake-pdf-content".toByteArray()

    @BeforeEach
    fun setUp() {
        // Build WebTestClient with controller and exception handler
        webTestClient = WebTestClient.bindToController(controller)
            .controllerAdvice(ResumeExceptionHandler())
            .apply { csrf() }
            .build()
            .mutateWith(csrf())

        // Mock handler to return PDF bytes
        coEvery { handler.handle(any<GenerateResumeCommand>()) } returns Mono.just(ByteArrayInputStream(pdfBytes))
    }

    @Test
    fun `should generate PDF resume and return 200 with application pdf content type`() {
        // Act & Assert
        webTestClient.post()
            .uri("/api/resumes")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept-Language", "en")
            .bodyValue(
                mapOf(
                    "personalInfo" to mapOf(
                        "fullName" to "John Doe",
                        "email" to "john.doe@example.com",
                        "phone" to "+1234567890",
                        "location" to "New York, NY, US",
                        "summary" to "Experienced software engineer",
                    ),
                    "workExperience" to listOf(
                        mapOf(
                            "company" to "Tech Corp",
                            "position" to "Senior Developer",
                            "startDate" to "2020-01-01",
                            "endDate" to null,
                            "description" to "Leading development team",
                        ),
                    ),
                    "education" to listOf(
                        mapOf(
                            "institution" to "MIT",
                            "degree" to "BSc Computer Science",
                            "startDate" to "2016-09-01",
                            "endDate" to "2020-06-01",
                        ),
                    ),
                    "skills" to listOf(
                        mapOf(
                            "name" to "Programming Languages",
                            "keywords" to listOf("Kotlin", "Java", "Python"),
                        ),
                    ),
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_PDF)
    }

    @Test
    fun `should return 400 when resume data is missing required fields`() {
        // Act & Assert
        webTestClient.post()
            .uri("/api/resumes")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept-Language", "en")
            .bodyValue(
                mapOf(
                    "personalInfo" to mapOf(
                        "fullName" to "",
                        "email" to "invalid-email",
                        "phone" to "",
                    ),
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
    }

    @Test
    fun `should use English locale when Accept-Language header is not provided`() {
        // Act & Assert
        webTestClient.post()
            .uri("/api/resumes")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                mapOf(
                    "personalInfo" to mapOf(
                        "fullName" to "John Doe",
                        "email" to "john.doe@example.com",
                        "phone" to "+1234567890",
                    ),
                    "workExperience" to listOf(
                        mapOf(
                            "company" to "Tech Corp",
                            "position" to "Senior Developer",
                            "startDate" to "2020-01-01",
                        ),
                    ),
                    "skills" to listOf(
                        mapOf(
                            "name" to "Programming",
                            "keywords" to listOf("Kotlin"),
                        ),
                    ),
                ),
            )
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should handle Spanish locale from Accept-Language header`() {
        // Act & Assert
        webTestClient.post()
            .uri("/api/resumes")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept-Language", "es")
            .bodyValue(
                mapOf(
                    "personalInfo" to mapOf(
                        "fullName" to "Juan Pérez",
                        "email" to "juan.perez@example.com",
                        "phone" to "+34123456789",
                    ),
                    "education" to listOf(
                        mapOf(
                            "institution" to "Universidad Complutense",
                            "degree" to "Ingeniería Informática",
                            "startDate" to "2016-09-01",
                            "endDate" to "2020-06-01",
                        ),
                    ),
                    "skills" to listOf(
                        mapOf(
                            "name" to "Lenguajes",
                            "keywords" to listOf("Kotlin", "Java"),
                        ),
                    ),
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_PDF)
    }
}
