package com.loomify.resume.infrastructure.web

import com.loomify.UnitTest
import com.loomify.resume.domain.exception.InvalidResumeDataException
import com.loomify.resume.domain.exception.LaTeXInjectionException
import com.loomify.resume.domain.exception.PdfGenerationException
import com.loomify.resume.domain.exception.PdfGenerationTimeoutException
import com.loomify.resume.domain.exception.TemplateRenderingException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.web.server.ServerWebExchange
import java.util.Locale

@UnitTest
internal class ResumeExceptionHandlerTest {

    private val messageSource = mockk<MessageSource>()
    private val handler = ResumeExceptionHandler(messageSource)
    private val exchange = mockk<ServerWebExchange>()
    private val requestMock = mockk<org.springframework.http.server.reactive.ServerHttpRequest>()

    @Test
    fun `should handle InvalidResumeDataException with localized message and trace ID`() {
        val exception = InvalidResumeDataException("Invalid data")
        
        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id-123"
        every { messageSource.getMessage("resume.error.invalid_data", null, any<Locale>()) } returns "Invalid resume data"
        
        val problemDetail = handler.handleInvalidResumeDataException(exception, exchange)

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.status)
        assertEquals("Invalid Resume Data", problemDetail.title)
        assertNotNull(problemDetail.properties?.get("localizedMessage"))
        assertEquals("Invalid resume data", problemDetail.properties?.get("localizedMessage"))
        assertNotNull(problemDetail.properties?.get("message"))
        assertEquals("resume.error.invalid_data", problemDetail.properties?.get("message"))
        assertNotNull(problemDetail.properties?.get("traceId"))
        assertEquals("test-trace-id-123", problemDetail.properties?.get("traceId"))
    }

    @Test
    fun `should handle TemplateRenderingException with localized message and trace ID`() {
        val exception = TemplateRenderingException("Template error")
        
        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id-456"
        every { messageSource.getMessage("resume.error.template_rendering", null, any<Locale>()) } returns "Failed to render resume template"
        
        val problemDetail = handler.handleTemplateRenderingException(exception, exchange)

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), problemDetail.status)
        assertEquals("Template Rendering Error", problemDetail.title)
        assertNotNull(problemDetail.properties?.get("localizedMessage"))
        assertEquals("Failed to render resume template", problemDetail.properties?.get("localizedMessage"))
        assertNotNull(problemDetail.properties?.get("message"))
        assertEquals("resume.error.template_rendering", problemDetail.properties?.get("message"))
        assertNotNull(problemDetail.properties?.get("traceId"))
        assertEquals("test-trace-id-456", problemDetail.properties?.get("traceId"))
    }

    @Test
    fun `should handle PdfGenerationException with localized message and trace ID`() {
        val exception = PdfGenerationException("PDF error")
        
        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id-789"
        every { messageSource.getMessage("resume.error.pdf_generation", null, any<Locale>()) } returns "Failed to generate PDF"
        
        val problemDetail = handler.handlePdfGenerationException(exception, exchange)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.status)
        assertEquals("PDF Generation Error", problemDetail.title)
        assertNotNull(problemDetail.properties?.get("localizedMessage"))
        assertEquals("Failed to generate PDF", problemDetail.properties?.get("localizedMessage"))
        assertNotNull(problemDetail.properties?.get("message"))
        assertEquals("resume.error.pdf_generation", problemDetail.properties?.get("message"))
        assertNotNull(problemDetail.properties?.get("traceId"))
        assertEquals("test-trace-id-789", problemDetail.properties?.get("traceId"))
    }

    @Test
    fun `should handle PdfGenerationTimeoutException with localized message and trace ID`() {
        val exception = PdfGenerationTimeoutException("Timeout")
        
        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id-timeout"
        every { messageSource.getMessage("resume.error.pdf_timeout", null, any<Locale>()) } returns "PDF generation timed out. Please try again with simpler content."
        
        val problemDetail = handler.handlePdfGenerationTimeoutException(exception, exchange)

        assertEquals(HttpStatus.GATEWAY_TIMEOUT.value(), problemDetail.status)
        assertEquals("PDF Generation Timeout", problemDetail.title)
        assertNotNull(problemDetail.properties?.get("localizedMessage"))
        assertNotNull(problemDetail.properties?.get("message"))
        assertEquals("resume.error.pdf_timeout", problemDetail.properties?.get("message"))
        assertNotNull(problemDetail.properties?.get("traceId"))
        assertEquals("test-trace-id-timeout", problemDetail.properties?.get("traceId"))
    }

    @Test
    fun `should handle LaTeXInjectionException with localized message and trace ID`() {
        val exception = LaTeXInjectionException("Malicious content detected")
        
        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id-security"
        every { messageSource.getMessage("resume.error.malicious_content", null, any<Locale>()) } returns "Content contains potentially unsafe characters"
        
        val problemDetail = handler.handleLaTeXInjectionException(exception, exchange)

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.status)
        assertEquals("Malicious Content Detected", problemDetail.title)
        assertNotNull(problemDetail.properties?.get("localizedMessage"))
        assertEquals("Content contains potentially unsafe characters", problemDetail.properties?.get("localizedMessage"))
        assertNotNull(problemDetail.properties?.get("message"))
        assertEquals("resume.error.malicious_content", problemDetail.properties?.get("message"))
        assertNotNull(problemDetail.properties?.get("traceId"))
        assertEquals("test-trace-id-security", problemDetail.properties?.get("traceId"))
    }

    @Test
    fun `should handle generic Exception with localized message and trace ID`() {
        val exception = Exception("Unexpected error")
        
        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id-generic"
        every { messageSource.getMessage("error.internal_server_error", null, any<Locale>()) } returns "An unexpected error occurred"
        
        val problemDetail = handler.handleGenericException(exception, exchange)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.status)
        assertEquals("Internal Server Error", problemDetail.title)
        assertNotNull(problemDetail.properties?.get("localizedMessage"))
        assertEquals("An unexpected error occurred", problemDetail.properties?.get("localizedMessage"))
        assertNotNull(problemDetail.properties?.get("message"))
        assertEquals("error.internal_server_error", problemDetail.properties?.get("message"))
        assertNotNull(problemDetail.properties?.get("traceId"))
        assertEquals("test-trace-id-generic", problemDetail.properties?.get("traceId"))
    }
}
