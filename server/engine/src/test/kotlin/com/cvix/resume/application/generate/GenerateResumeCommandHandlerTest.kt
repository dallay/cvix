package com.cvix.resume.application.generate

import com.cvix.UnitTest
import com.cvix.resume.ResumeTestFixtures
import com.cvix.resume.domain.Locale
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
internal class GenerateResumeCommandHandlerTest {
    private lateinit var generateResumeCommandHandler: GenerateResumeCommandHandler
    private val pdfGenerator: PdfResumeGenerator = mockk()

    private val testUserId = UUID.randomUUID()
    private val testTemplateId = "engineering-template"

    @BeforeEach
    fun setUp() {
        generateResumeCommandHandler = GenerateResumeCommandHandler(pdfGenerator)
    }

    @Test
    fun `should generate PDF resume when handle is called with valid command`() = runTest {
        // Given
        val resume = ResumeTestFixtures.createValidResume()
        val command = GenerateResumeCommand(
            templateId = testTemplateId,
            resume = resume,
            userId = testUserId,
            locale = Locale.EN,
        )
        val expectedPdfStream: InputStream = ByteArrayInputStream("PDF content".toByteArray())

        coEvery {
            pdfGenerator.generate(
                testTemplateId,
                resume,
                testUserId,
                Locale.EN,
            )
        } returns expectedPdfStream

        // When
        val result = generateResumeCommandHandler.handle(command)

        // Then
        assertNotNull(result)
        assertEquals(expectedPdfStream, result)
        coVerify { pdfGenerator.generate(testTemplateId, resume, testUserId, Locale.EN) }
    }

    @Test
    fun `should generate PDF resume with Spanish locale when specified`() = runTest {
        // Given
        val resume = ResumeTestFixtures.createValidResume()
        val command = GenerateResumeCommand(
            templateId = testTemplateId,
            resume = resume,
            userId = testUserId,
            locale = Locale.ES,
        )
        val expectedPdfStream: InputStream = ByteArrayInputStream("PDF content".toByteArray())

        coEvery {
            pdfGenerator.generate(
                testTemplateId,
                resume,
                testUserId,
                Locale.ES,
            )
        } returns expectedPdfStream

        // When
        val result = generateResumeCommandHandler.handle(command)

        // Then
        assertNotNull(result)
        assertEquals(expectedPdfStream, result)
        coVerify { pdfGenerator.generate(testTemplateId, resume, testUserId, Locale.ES) }
    }

    @Test
    fun `should throw IllegalArgumentException when locale is invalid`() = runTest {
        // Given
        val resume = ResumeTestFixtures.createValidResume()
        val command = GenerateResumeCommand(
            templateId = testTemplateId,
            resume = resume,
            userId = testUserId,
            locale = Locale.EN,
        )

        coEvery {
            pdfGenerator.generate(
                any(),
                any(),
                any(),
                any(),
            )
        } throws IllegalArgumentException("Unsupported locale")

        // When / Then
        assertThrows<IllegalArgumentException> {
            generateResumeCommandHandler.handle(command)
        }
    }
}
