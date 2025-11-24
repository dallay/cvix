package com.loomify.resume.infrastructure.http

import com.loomify.ControllerIntegrationTest
import com.loomify.resume.ResumeTestFixtures
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.jdbc.Sql

internal class ResumeGeneratorControllerIntegrationTest : ControllerIntegrationTest() {
    @Test
    @Sql(
        "/db/user/users.sql",
        "/db/workspace/workspace.sql",
    )
    @Sql(
        "/db/workspace/clean.sql",
        "/db/user/clean.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    )
    fun `should generate resume PDF successfully`() {
        val request = ResumeTestFixtures.createValidResumeRequestContent()

        webTestClient.mutateWith(csrf()).post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_PDF)
            .expectHeader().exists(HttpHeaders.CONTENT_DISPOSITION)
            .expectHeader().exists("X-Generation-Time-Ms")
    }

    @Test
    @Sql(
        "/db/user/users.sql",
        "/db/workspace/workspace.sql",
    )
    @Sql(
        "/db/workspace/clean.sql",
        "/db/user/clean.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    )
    fun `should generate resume with Spanish locale`() {
        val request = ResumeTestFixtures.createValidResumeRequestContent()

        webTestClient.mutateWith(csrf()).post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_LANGUAGE, "es")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_PDF)
            .expectHeader().valueEquals("Content-Language", "es")
            .expectBody().consumeWith { response ->
                val pdfBytes = response.responseBody
                requireNotNull(pdfBytes) { "PDF response body is null" }
                PDDocument.load(pdfBytes.inputStream()).use { doc ->
                    val text = PDFTextStripper().getText(doc)
                    // Assert at least one known Spanish label is present
                    assert(text.contains("Resumen") || text.contains("Experiencia")) {
                        "PDF does not contain expected Spanish localization: $text"
                    }
                }
            }
    }

    @Test
    @Sql(
        "/db/user/users.sql",
        "/db/workspace/workspace.sql",
    )
    @Sql(
        "/db/workspace/clean.sql",
        "/db/user/clean.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    )
    fun `should return 400 for invalid request data`() {
        val invalidRequest = mapOf("invalid" to "data")

        webTestClient.mutateWith(csrf()).post()
            .uri("/api/resume/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.title").isEqualTo("Bad Request")
            .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
            .jsonPath("$.detail").isEqualTo("Failed to read HTTP message")
            .jsonPath("$.instance").isEqualTo("/api/resume/generate")
    }
}
