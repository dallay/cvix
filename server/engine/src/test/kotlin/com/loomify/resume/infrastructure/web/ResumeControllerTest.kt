package com.loomify.resume.infrastructure.web

import com.loomify.UnitTest
import com.loomify.engine.authentication.infrastructure.ApplicationSecurityProperties
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
    private val securityProperties = ApplicationSecurityProperties()
    private val controller = ResumeController(handler, securityProperties)
    private lateinit var webTestClient: WebTestClient
    private val pdfBytes = "fake-pdf-content".toByteArray()

    @Test
    fun `should return 400 when project startDate is not ISO format`() {
        webTestClient.post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                mapOf(
                    "basics" to mapOf(
                        "name" to "John Doe",
                        "email" to "john.doe@example.com",
                        "phone" to "+1234567890",
                    ),
                    "projects" to listOf(
                        mapOf(
                            "name" to "Test Project",
                            "description" to "desc",
                            "startDate" to "2020/01/01", // invalid format
                        ),
                    ),
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.fieldErrors[?(@.field=='projects[0].startDate')].message")
            .isEqualTo("startDate must be ISO yyyy-MM-dd")
    }

    @Test
    fun `should return 400 when project endDate is not ISO format`() {
        webTestClient.post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                mapOf(
                    "basics" to mapOf(
                        "name" to "John Doe",
                        "email" to "john.doe@example.com",
                        "phone" to "+1234567890",
                    ),
                    "projects" to listOf(
                        mapOf(
                            "name" to "Test Project",
                            "description" to "desc",
                            "endDate" to "2020/06/01", // invalid format
                        ),
                    ),
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.fieldErrors[?(@.field=='projects[0].endDate')].message")
            .isEqualTo("endDate must be ISO yyyy-MM-dd")
    }

    @BeforeEach
    fun setUp() {
        // Build WebTestClient with controller and exception handler
        webTestClient = WebTestClient.bindToController(controller)
            .controllerAdvice(ResumeExceptionHandler())
            .apply { csrf() }
            .build()
            .mutateWith(csrf())

        // Mock handler to return PDF bytes
        coEvery { handler.handle(any<GenerateResumeCommand>()) } returns Mono.just(
            ByteArrayInputStream(pdfBytes),
        )
    }

    @Test
    fun `should generate PDF resume and return 200 with application pdf content type`() {
        // Act & Assert
        webTestClient.post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept-Language", "en")
            .bodyValue(
                mapOf(
                    "basics" to mapOf(
                        "name" to "John Doe",
                        "email" to "john.doe@example.com",
                        "phone" to "+1234567890",
                        "location" to mapOf(
                            "city" to "New York",
                            "region" to "NY",
                            "countryCode" to "US",
                        ),
                        "summary" to "Experienced software engineer",
                    ),
                    "work" to listOf(
                        mapOf(
                            "name" to "Tech Corp",
                            "position" to "Senior Developer",
                            "startDate" to "2020-01-01",
                            "endDate" to null,
                            "description" to "Leading development team",
                        ),
                    ),
                    "education" to listOf(
                        mapOf(
                            "institution" to "MIT",
                            "area" to "Computer Science",
                            "studyType" to "BSc",
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
            .expectHeader()
            .valueEquals("Content-Security-Policy", securityProperties.contentSecurityPolicy)
            .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
            .expectHeader().valueEquals("X-Frame-Options", "DENY")
            .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin")
    }

    @Test
    fun `should return 400 when resume data is missing required fields`() {
        // Act & Assert
        webTestClient.post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept-Language", "en")
            .bodyValue(
                mapOf(
                    "basics" to mapOf(
                        "name" to "",
                        "email" to "invalid-email",
                        "phone" to "",
                    ),
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.title").exists()
            .jsonPath("$.detail").exists()
    }

    @Test
    fun `should use English locale when Accept-Language header is not provided`() {
        // Act & Assert
        webTestClient.post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                mapOf(
                    "basics" to mapOf(
                        "name" to "John Doe",
                        "email" to "john.doe@example.com",
                        "phone" to "+1234567890",
                    ),
                    "work" to listOf(
                        mapOf(
                            "name" to "Tech Corp",
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
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept-Language", "es")
            .bodyValue(
                mapOf(
                    "basics" to mapOf(
                        "name" to "Juan Pérez",
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

    @Test
    fun `should use primary language subtag when Accept-Language has region`() {
        // Act & Assert
        webTestClient.post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept-Language", "en-US")
            .bodyValue(
                mapOf(
                    "basics" to mapOf(
                        "name" to "John Doe",
                        "email" to "john.doe@example.com",
                        "phone" to "+1234567890",
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
            .expectHeader().contentType(MediaType.APPLICATION_PDF)
    }

    @Test
    fun `should accept resume with new JSON Resume Schema sections`() {
        // Mock the handler
        coEvery { handler.handle(any()) } returns Mono.just(ByteArrayInputStream(pdfBytes))

        webTestClient.mutateWith(csrf()).post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                mapOf(
                    "basics" to mapOf(
                        "name" to "John Doe",
                        "label" to "Software Engineer",
                        "image" to "https://example.com/photo.jpg",
                        "email" to "john@example.com",
                        "phone" to "+1234567890",
                    ),
                    "skills" to listOf(
                        mapOf(
                            "name" to "Programming",
                            "level" to "Expert",
                            "keywords" to listOf("Kotlin", "Java"),
                        ),
                    ),
                    "volunteer" to listOf(
                        mapOf(
                            "organization" to "Code.org",
                            "position" to "Volunteer Teacher",
                            "summary" to "Teaching programming to kids",
                        ),
                    ),
                    "awards" to listOf(
                        mapOf(
                            "title" to "Best Developer",
                            "date" to "2024-01-01",
                            "awarder" to "Tech Company",
                        ),
                    ),
                    "certificates" to listOf(
                        mapOf(
                            "name" to "AWS Certified",
                            "date" to "2024-01-01",
                            "issuer" to "Amazon",
                        ),
                    ),
                    "publications" to listOf(
                        mapOf(
                            "name" to "Clean Code Principles",
                            "publisher" to "Tech Blog",
                        ),
                    ),
                    "interests" to listOf(
                        mapOf(
                            "name" to "Open Source",
                            "keywords" to listOf("Kotlin", "Spring Boot"),
                        ),
                    ),
                    "references" to listOf(
                        mapOf(
                            "name" to "Jane Smith",
                            "reference" to "John is an excellent developer",
                        ),
                    ),
                    "projects" to listOf(
                        mapOf(
                            "name" to "My Project",
                            "description" to "A great project",
                            "highlights" to listOf("Feature 1", "Feature 2"),
                            "keywords" to listOf("Kotlin", "Spring"),
                            "roles" to listOf("Developer", "Architect"),
                            "entity" to "My Company",
                            "type" to "application",
                        ),
                    ),
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_PDF)
    }

    @Test
    fun `should accept resume with free-form language fluency`() {
        // Mock the handler
        coEvery { handler.handle(any()) } returns Mono.just(ByteArrayInputStream(pdfBytes))

        webTestClient.mutateWith(csrf()).post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                mapOf(
                    "basics" to mapOf(
                        "name" to "John Doe",
                        "email" to "john@example.com",
                        "phone" to "+1234567890",
                    ),
                    "skills" to listOf(
                        mapOf(
                            "name" to "Programming",
                            "keywords" to listOf("Kotlin"),
                        ),
                    ),
                    "languages" to listOf(
                        mapOf(
                            "language" to "English",
                            "fluency" to "Native speaker",
                        ),
                        mapOf(
                            "language" to "Spanish",
                            "fluency" to "Professional working proficiency",
                        ),
                    ),
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_PDF)
    }

    @Test
    fun `should accept resume with structured profiles array`() {
        // Mock the handler
        coEvery { handler.handle(any()) } returns Mono.just(ByteArrayInputStream(pdfBytes))

        webTestClient.mutateWith(csrf()).post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                mapOf(
                    "basics" to mapOf(
                        "name" to "John Doe",
                        "email" to "john@example.com",
                        "phone" to "+1234567890",
                        "location" to mapOf(
                            "address" to "123 Main St",
                            "city" to "San Francisco",
                            "region" to "California",
                            "countryCode" to "US",
                            "postalCode" to "94102",
                        ),
                        "profiles" to listOf(
                            mapOf(
                                "network" to "LinkedIn",
                                "username" to "johndoe",
                                "url" to "https://linkedin.com/in/johndoe",
                            ),
                            mapOf(
                                "network" to "GitHub",
                                "username" to "johndoe",
                                "url" to "https://github.com/johndoe",
                            ),
                            mapOf(
                                "network" to "Twitter",
                                "username" to "johndoe",
                                "url" to "https://twitter.com/johndoe",
                            ),
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
            .expectHeader().contentType(MediaType.APPLICATION_PDF)
    }
}
