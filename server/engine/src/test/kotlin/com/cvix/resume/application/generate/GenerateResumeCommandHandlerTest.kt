package com.cvix.resume.application.generate

import com.cvix.UnitTest
import com.cvix.resume.ResumeTestFixtures
import com.cvix.resume.domain.Locale
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.io.InputStream
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

    @BeforeEach
    fun setUp() {
        generateResumeCommandHandler = GenerateResumeCommandHandler(pdfGenerator)
    }

    @Test
    fun `should generate PDF resume when handle is called with valid command`() = runTest {
        // Given
        val resume = ResumeTestFixtures.createValidResume()
        val command = GenerateResumeCommand(resume = resume, locale = Locale.EN)
        val expectedPdfStream: InputStream = ByteArrayInputStream("PDF content".toByteArray())

        coEvery { pdfGenerator.generate(eq(resume), eq(Locale.EN)) } returns expectedPdfStream

        // When
        val result = generateResumeCommandHandler.handle(command)

        // Then
        assertNotNull(result)
        assertEquals(expectedPdfStream, result)
        coVerify { pdfGenerator.generate(eq(resume), eq(Locale.EN)) }
    }

    @Test
    fun `should generate PDF resume with Spanish locale when specified`() = runTest {
        // Given
        val resume = ResumeTestFixtures.createValidResume()
        val command = GenerateResumeCommand(resume = resume, locale = Locale.ES)
        val expectedPdfStream: InputStream = ByteArrayInputStream("PDF content".toByteArray())

        coEvery { pdfGenerator.generate(eq(resume), eq(Locale.ES)) } returns expectedPdfStream

        // When
        val result = generateResumeCommandHandler.handle(command)

        // Then
        assertNotNull(result)
        assertEquals(expectedPdfStream, result)
        coVerify { pdfGenerator.generate(eq(resume), eq(Locale.ES)) }
    }

    @Test
    fun `should throw IllegalArgumentException when locale is invalid`() = runTest {
        // Given
        val resume = ResumeTestFixtures.createValidResume()
        val command = GenerateResumeCommand(resume = resume, locale = Locale.EN)

        coEvery { pdfGenerator.generate(any(), any()) } throws IllegalArgumentException("Unsupported locale")

        // When / Then
        assertThrows<IllegalArgumentException> {
            generateResumeCommandHandler.handle(command)
        }
    }
}
