package com.loomify.resume.application.handler

import com.loomify.UnitTest
import com.loomify.resume.application.command.GenerateResumeCommand
import com.loomify.resume.domain.model.CompanyName
import com.loomify.resume.domain.model.FullName
import com.loomify.resume.domain.model.JobTitle
import com.loomify.resume.domain.model.PersonalInfo
import com.loomify.resume.domain.model.ResumeData
import com.loomify.resume.domain.model.WorkExperience
import com.loomify.resume.domain.port.PdfGeneratorPort
import com.loomify.resume.domain.port.TemplateRendererPort
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.ByteArrayInputStream
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Unit tests for GenerateResumeCommandHandler.
 *
 * Tests orchestration logic:
 * - Resume data validation
 * - Template rendering with correct locale
 * - PDF generation
 * - Error handling
 */
@UnitTest
class GenerateResumeCommandHandlerTest {

    private val templateRenderer: TemplateRendererPort = mockk()
    private val pdfGenerator: PdfGeneratorPort = mockk()
    private val handler = GenerateResumeCommandHandler(templateRenderer, pdfGenerator)

    @Test
    fun `should generate PDF successfully with English locale`() {
        // Arrange
        val resumeData = createValidResumeData()
        val command = GenerateResumeCommand(resumeData, "en")
        val latexSource = "\\documentclass{article}..."
        val pdfBytes = "PDF content".toByteArray()

        coEvery { templateRenderer.render(resumeData, "en") } returns latexSource
        coEvery { pdfGenerator.generatePdf(latexSource, "en") } returns
            Mono.just(ByteArrayInputStream(pdfBytes))

        // Act
        val result = handler.handle(command)

        // Assert
        StepVerifier.create(result)
            .assertNext { inputStream ->
                inputStream shouldNotBe null
                inputStream.readAllBytes() shouldBe pdfBytes
            }
            .verifyComplete()

        coVerify(exactly = 1) { templateRenderer.render(resumeData, "en") }
        coVerify(exactly = 1) { pdfGenerator.generatePdf(latexSource, "en") }
    }

    @Test
    fun `should generate PDF successfully with Spanish locale`() {
        // Arrange
        val resumeData = createValidResumeData()
        val command = GenerateResumeCommand(resumeData, "es")
        val latexSource = "\\documentclass{article}..."
        val pdfBytes = "PDF content".toByteArray()

        coEvery { templateRenderer.render(resumeData, "es") } returns latexSource
        coEvery { pdfGenerator.generatePdf(latexSource, "es") } returns
            Mono.just(ByteArrayInputStream(pdfBytes))

        // Act
        val result = handler.handle(command)

        // Assert
        StepVerifier.create(result)
            .assertNext { inputStream ->
                inputStream shouldNotBe null
            }
            .verifyComplete()

        coVerify(exactly = 1) { templateRenderer.render(resumeData, "es") }
        coVerify(exactly = 1) { pdfGenerator.generatePdf(latexSource, "es") }
    }

    @Test
    fun `should default to English locale when not specified`() {
        // Arrange
        val resumeData = createValidResumeData()
        val command = GenerateResumeCommand(resumeData) // Uses default "en" locale
        val latexSource = "\\documentclass{article}..."
        val pdfBytes = "PDF content".toByteArray()

        coEvery { templateRenderer.render(resumeData, "en") } returns latexSource
        coEvery { pdfGenerator.generatePdf(latexSource, "en") } returns
            Mono.just(ByteArrayInputStream(pdfBytes))

        // Act
        val result = handler.handle(command)

        // Assert
        StepVerifier.create(result)
            .expectNextCount(1)
            .verifyComplete()

        coVerify(exactly = 1) { templateRenderer.render(resumeData, "en") }
    }

    @Test
    fun `should propagate template rendering errors`() {
        // Arrange
        val resumeData = createValidResumeData()
        val command = GenerateResumeCommand(resumeData, "en")

        coEvery { templateRenderer.render(any(), any()) } throws
            RuntimeException("Template rendering failed")

        // Act
        val result = handler.handle(command)

        // Assert
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should propagate PDF generation errors`() {
        // Arrange
        val resumeData = createValidResumeData()
        val command = GenerateResumeCommand(resumeData, "en")
        val latexSource = "\\documentclass{article}..."

        coEvery { templateRenderer.render(resumeData, "en") } returns latexSource
        coEvery { pdfGenerator.generatePdf(latexSource, "en") } returns
            Mono.error(RuntimeException("PDF generation failed"))

        // Act
        val result = handler.handle(command)

        // Assert
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    // Helper function to create valid test data
    private fun createValidResumeData() = ResumeData(
        basics = PersonalInfo(
            fullName = FullName("John Doe"),
            label = JobTitle("Software Engineer"),
            email = com.loomify.common.domain.vo.email.Email("john@example.com"),
            phone = null,
            url = null,
            summary = null,
            location = null,
            profiles = emptyList(),
        ),
        work = listOf(
            WorkExperience(
                company = CompanyName("ACME Corp"),
                position = JobTitle("Developer"),
                startDate = "2020-01-01",
                endDate = "2023-12-31",
                location = null,
                summary = null,
                highlights = null,
                url = null,
            ),
        ),
        education = emptyList(),
        skills = emptyList(),
    )
}
