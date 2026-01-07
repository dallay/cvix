package com.cvix.resume.infrastructure.http

import com.cvix.ControllerIntegrationTest
import com.cvix.resume.ResumeTestFixtures
import com.cvix.resume.infrastructure.pdf.DockerPdfGenerator
import java.util.concurrent.TimeUnit
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.jdbc.Sql

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ResumeGeneratorControllerIntegrationTest : ControllerIntegrationTest() {

    @Autowired
    private lateinit var dockerPdfGenerator: DockerPdfGenerator

    @BeforeAll
    fun waitForDockerImage() {
        // Wait for Docker image to be pulled during startup (up to 20 minutes in CI)
        // This prevents timeout issues during test execution, especially in resource-constrained CI environments
        val startTime = System.currentTimeMillis()
        val maxWaitMillis = TimeUnit.MINUTES.toMillis(20)

        // The PostConstruct method starts the pull in a background thread
        // Wait for it to complete by checking if we can generate a minimal PDF
        var attempts = 0
        var lastException: Exception? = null
        while (System.currentTimeMillis() - startTime < maxWaitMillis) {
            try {
                attempts++
                // Try to generate a simple test PDF to verify the image is ready
                val testLatex = """
                    \documentclass{article}
                    \begin{document}
                    Test
                    \end{document}
                """.trimIndent()

                // This will trigger image pull if not done yet
                dockerPdfGenerator.generatePdf(testLatex, "en").block()
                println("Docker image ready after $attempts attempts (${System.currentTimeMillis() - startTime}ms)")
                return
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                lastException = e
                // Image not ready yet or pull in progress, wait and retry
                if (attempts % 6 == 0) { // Log every minute
                    logDockerWaitProgress(attempts, startTime, e)
                }
                Thread.sleep(10_000) // Wait 10 seconds between attempts
            }
        }

        // Log final error details before throwing
        logDockerTestFailureDetails(attempts, startTime, lastException)

        throw IllegalStateException(
            "Docker image not ready after ${maxWaitMillis / 1000} seconds. " +
                "Please ensure Docker is running and has sufficient resources. " +
                "Last error: ${lastException?.message}",
            lastException,
        )
    }

    private fun logDockerWaitProgress(attempts: Int, startTime: Long, exception: Exception) {
        println(
            "Waiting for Docker image pull to complete... " +
                "(attempt $attempts, ${(System.currentTimeMillis() - startTime) / 1000}s elapsed)",
        )
        println("Last error: ${exception.javaClass.simpleName}: ${exception.message}")
        exception.cause?.let { cause ->
            println("Caused by: ${cause.javaClass.simpleName}: ${cause.message}")
        }
    }

    private fun logDockerTestFailureDetails(attempts: Int, startTime: Long, lastException: Exception?) {
        println("=== Docker Integration Test Failure Details ===")
        println("Total attempts: $attempts")
        println("Total wait time: ${(System.currentTimeMillis() - startTime) / 1000}s")
        lastException?.let { e ->
            println("Final exception: ${e.javaClass.name}: ${e.message}")
            e.cause?.let { cause ->
                println("Caused by: ${cause.javaClass.name}: ${cause.message}")
            }
            e.printStackTrace()
        }
        println("=== End of Docker Integration Test Failure Details ===")
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
    @Timeout(150) // Increased to 150s to accommodate Docker image pulls in CI
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
    @Timeout(150) // Increased to 150s to accommodate Docker image pulls in CI
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
            .jsonPath("$.title").isEqualTo("Invalid Input")
            .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
            .jsonPath("$.instance").isEqualTo("/api/resume/generate")
    }
}
