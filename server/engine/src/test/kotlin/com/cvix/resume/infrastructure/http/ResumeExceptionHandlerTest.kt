package com.cvix.resume.infrastructure.http

import com.cvix.UnitTest
import com.cvix.resume.domain.exception.InvalidResumeDataException
import com.cvix.resume.domain.exception.LaTeXInjectionException
import com.cvix.resume.domain.exception.PdfGenerationException
import com.cvix.resume.domain.exception.PdfGenerationTimeoutException
import com.cvix.resume.domain.exception.TemplateRenderingException
import io.mockk.every
import io.mockk.mockk
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.web.server.ServerWebExchange

@UnitTest
internal class ResumeExceptionHandlerTest {

    private val messageSource = mockk<MessageSource>()
    private val handler = ResumeExceptionHandler(messageSource)
    private val exchange = mockk<ServerWebExchange>(relaxed = true)
    private val requestMock = mockk<org.springframework.http.server.reactive.ServerHttpRequest>()
    private val localeContextMock = mockk<org.springframework.context.i18n.LocaleContext>()

    @Test
    fun `should handle InvalidResumeDataException with localized message and trace ID`() {
        val exception = InvalidResumeDataException("Invalid data")

        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id-123"
        every { exchange.localeContext } returns localeContextMock
        every { localeContextMock.locale } returns Locale.ENGLISH
        every {
            messageSource.getMessage(
                "resume.error.invalid_data",
                null,
                "resume.error.invalid_data",
                any<Locale>(),
            )
        } returns "Invalid resume data"

        val problemDetail = handler.handleInvalidResumeDataException(exception, exchange)

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.status)
        assertEquals("Invalid Resume Data", problemDetail.title as Any)
        assertNotNull(problemDetail.properties?.get("localizedMessage") as Any)
        assertEquals(
            "Invalid resume data",
            problemDetail.properties?.get("localizedMessage") as Any,
        )
        assertNotNull(problemDetail.properties?.get("message") as Any)
        assertEquals("resume.error.invalid_data", problemDetail.properties?.get("message") as Any)
        assertNotNull(problemDetail.properties?.get("traceId") as Any)
        assertEquals("test-trace-id-123", problemDetail.properties?.get("traceId") as Any)
    }

    @Test
    fun `should handle TemplateRenderingException with localized message and trace ID`() {
        val exception = TemplateRenderingException("Template error")

        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id-456"
        every { exchange.localeContext } returns localeContextMock
        every { localeContextMock.locale } returns Locale.ENGLISH
        every {
            messageSource.getMessage(
                "resume.error.template_rendering",
                null,
                "resume.error.template_rendering",
                any<Locale>(),
            )
        } returns "Failed to render resume template"

        val problemDetail = handler.handleTemplateRenderingException(exception, exchange)

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), problemDetail.status)
        assertEquals("Template Rendering Error", problemDetail.title as Any)
        assertNotNull(problemDetail.properties?.get("localizedMessage") as Any)
        assertEquals(
            "Failed to render resume template",
            problemDetail.properties?.get("localizedMessage") as Any,
        )
        assertNotNull(problemDetail.properties?.get("message") as Any)
        assertEquals(
            "resume.error.template_rendering",
            problemDetail.properties?.get("message") as Any,
        )
        assertNotNull(problemDetail.properties?.get("traceId") as Any)
        assertEquals("test-trace-id-456", problemDetail.properties?.get("traceId") as Any)
    }

    @Test
    fun `should handle PdfGenerationException with localized message and trace ID`() {
        val exception = PdfGenerationException("PDF error")

        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id-789"
        every { exchange.localeContext } returns localeContextMock
        every { localeContextMock.locale } returns Locale.ENGLISH
        every {
            messageSource.getMessage(
                "resume.error.pdf_generation",
                null,
                "resume.error.pdf_generation",
                any<Locale>(),
            )
        } returns "Failed to generate PDF"

        val problemDetail = handler.handlePdfGenerationException(exception, exchange)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.status)
        assertEquals("PDF Generation Error", problemDetail.title as Any)
        assertNotNull(problemDetail.properties?.get("localizedMessage") as Any)
        assertEquals(
            "Failed to generate PDF",
            problemDetail.properties?.get("localizedMessage") as Any,
        )
        assertNotNull(problemDetail.properties?.get("message") as Any)
        assertEquals("resume.error.pdf_generation", problemDetail.properties?.get("message") as Any)
        assertNotNull(problemDetail.properties?.get("traceId") as Any)
        assertEquals("test-trace-id-789", problemDetail.properties?.get("traceId") as Any)
    }

    @Test
    fun `should handle PdfGenerationTimeoutException with localized message and trace ID`() {
        val exception = PdfGenerationTimeoutException("Timeout")

        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id-timeout"
        every { exchange.localeContext } returns localeContextMock
        every { localeContextMock.locale } returns Locale.ENGLISH
        every {
            messageSource.getMessage(
                "resume.error.pdf_timeout",
                null,
                "resume.error.pdf_timeout",
                any<Locale>(),
            )
        } returns "PDF generation timed out. Please try again with simpler content."

        val problemDetail = handler.handlePdfGenerationTimeoutException(exception, exchange)

        assertEquals(HttpStatus.GATEWAY_TIMEOUT.value(), problemDetail.status)
        assertEquals("PDF Generation Timeout", problemDetail.title as Any)
        assertNotNull(problemDetail.properties?.get("localizedMessage") as Any)
        assertNotNull(problemDetail.properties?.get("message") as Any)
        assertEquals("resume.error.pdf_timeout", problemDetail.properties?.get("message") as Any)
        assertNotNull(problemDetail.properties?.get("traceId") as Any)
        assertEquals("test-trace-id-timeout", problemDetail.properties?.get("traceId") as Any)
    }

    @Test
    fun `should handle LaTeXInjectionException with localized message and trace ID`() {
        val exception = LaTeXInjectionException("Malicious content detected")

        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id-security"
        every { exchange.localeContext } returns localeContextMock
        every { localeContextMock.locale } returns Locale.ENGLISH
        every {
            messageSource.getMessage(
                "resume.error.malicious_content",
                null,
                "resume.error.malicious_content",
                any<Locale>(),
            )
        } returns "Content contains potentially unsafe characters"

        val problemDetail = handler.handleLaTeXInjectionException(exception, exchange)

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.status)
        assertEquals("Malicious Content Detected", problemDetail.title as Any)
        assertNotNull(problemDetail.properties?.get("localizedMessage") as Any)
        assertEquals(
            "Content contains potentially unsafe characters",
            problemDetail.properties?.get("localizedMessage") as Any,
        )
        assertNotNull(problemDetail.properties?.get("message") as Any)
        assertEquals(
            "resume.error.malicious_content",
            problemDetail.properties?.get("message") as Any,
        )
        assertNotNull(problemDetail.properties?.get("traceId") as Any)
        assertEquals("test-trace-id-security", problemDetail.properties?.get("traceId") as Any)
    }

    @Test
    fun `should handle generic Exception with localized message and trace ID`() {
        val exception = Exception("Unexpected error")

        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id-generic"
        every { exchange.localeContext } returns localeContextMock
        every { localeContextMock.locale } returns Locale.ENGLISH
        every {
            messageSource.getMessage(
                "error.internal_server_error",
                null,
                "error.internal_server_error",
                any<Locale>(),
            )
        } returns "An unexpected error occurred"

        val problemDetail = handler.handleGenericException(exception, exchange)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.status)
        assertEquals("Internal Server Error", problemDetail.title as Any)
        assertNotNull(problemDetail.properties?.get("localizedMessage") as Any)
        assertEquals(
            "An unexpected error occurred",
            problemDetail.properties?.get("localizedMessage") as Any,
        )
        assertNotNull(problemDetail.properties?.get("message") as Any)
        assertEquals("error.internal_server_error", problemDetail.properties?.get("message") as Any)
        assertNotNull(problemDetail.properties?.get("traceId") as Any)
        assertEquals("test-trace-id-generic", problemDetail.properties?.get("traceId") as Any)
    }
}
