package com.cvix.resume.infrastructure.http

import com.cvix.resume.domain.exception.InvalidResumeDataException
import com.cvix.resume.domain.exception.LaTeXInjectionException
import com.cvix.resume.domain.exception.PdfGenerationException
import com.cvix.resume.domain.exception.PdfGenerationTimeoutException
import com.cvix.resume.domain.exception.TemplateRenderingException
import java.net.URI
import java.time.Instant
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebExchange

/**
 * Global exception handler for resume generation endpoints.
 * Translates domain exceptions to HTTP responses with standardized ProblemDetail format.
 *
 * Error mappings per plan.md:
 * - InvalidResumeDataException → HTTP 400 Bad Request
 * - TemplateRenderingException → HTTP 422 Unprocessable Entity
 * - PdfGenerationException → HTTP 500 Internal Server Error
 * - PdfGenerationTimeoutException → HTTP 504 Gateway Timeout
 * - Rate limit exceeded → HTTP 429 Too Many Requests (handled by Bucket4j filter)
 */
@RestControllerAdvice("com.cvix.resume")
class ResumeExceptionHandler(
    private val messageSource: MessageSource
) {

    private val logger = LoggerFactory.getLogger(ResumeExceptionHandler::class.java)

    // Centralized locale/message resolution (reactive-safe)
    private fun getLocalizedMessage(exchange: ServerWebExchange, messageKey: String): String {
        val locale = exchange.localeContext.locale ?: Locale.getDefault()
        return messageSource.getMessage(messageKey, null, locale)
    }

    /**
     * Handle validation errors from Spring Validation (@Valid)
     */
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationException(
        ex: WebExchangeBindException,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val localizedMessage = getLocalizedMessage(exchange, MSG_VALIDATION_ERROR)

        val fieldErrors = ex.fieldErrors.map { error ->
            mapOf(
                "field" to error.field,
                "message" to (error.defaultMessage ?: "Invalid value"),
            )
        }

        val globalErrors = ex.bindingResult.globalErrors.map { error ->
            error.defaultMessage ?: "Validation failed"
        }

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Request validation failed. Please check field and global errors.",
        )
        problemDetail.title = "Validation Error"
        problemDetail.type = URI.create("$ERROR_PAGE/resume/validation-error")
        problemDetail.setProperty(ERROR_CATEGORY, "VALIDATION")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE, MSG_VALIDATION_ERROR)
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty("fieldErrors", fieldErrors)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)
        if (globalErrors.isNotEmpty()) {
            problemDetail.setProperty("errors", globalErrors)
        }

        logger.warn("Validation error: {} fields failed, {} global errors", fieldErrors.size, globalErrors.size)
        return problemDetail
    }

    /**
     * Handle invalid resume data (missing required sections, business rule violations)
     */
    @ExceptionHandler(InvalidResumeDataException::class)
    fun handleInvalidResumeDataException(
        ex: InvalidResumeDataException,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val localizedMessage = getLocalizedMessage(exchange, MSG_INVALID_DATA)

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.message ?: "Resume data validation failed",
        )
        problemDetail.title = "Invalid Resume Data"
        problemDetail.type = URI.create("$ERROR_PAGE/resume/invalid-data")
        problemDetail.setProperty(ERROR_CATEGORY, "INVALID_RESUME_DATA")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE, MSG_INVALID_DATA)
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)

        logger.warn("Invalid resume data: {}", ex.message)
        return problemDetail
    }

    /**
     * Handle template rendering errors (LaTeX syntax, missing data)
     */
    @ExceptionHandler(TemplateRenderingException::class)
    fun handleTemplateRenderingException(
        ex: TemplateRenderingException,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val localizedMessage = getLocalizedMessage(exchange, MSG_TEMPLATE_RENDERING)

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Failed to render resume template. Please check your data.",
        )
        problemDetail.title = "Template Rendering Error"
        problemDetail.type = URI.create("$ERROR_PAGE/resume/template-error")
        problemDetail.setProperty(ERROR_CATEGORY, "TEMPLATE_RENDERING")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE, MSG_TEMPLATE_RENDERING)
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)

        logger.error("Template rendering failed: {}", ex.message, ex)
        return problemDetail
    }

    /**
     * Handle PDF generation failures (Docker, LaTeX compilation)
     */
    @ExceptionHandler(PdfGenerationException::class)
    fun handlePdfGenerationException(
        ex: PdfGenerationException,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val localizedMessage = getLocalizedMessage(exchange, MSG_PDF_GENERATION)

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Failed to generate PDF. Please try again later.",
        )
        problemDetail.title = "PDF Generation Error"
        problemDetail.type = URI.create("$ERROR_PAGE/resume/pdf-generation-error")
        problemDetail.setProperty(ERROR_CATEGORY, "PDF_GENERATION")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE, MSG_PDF_GENERATION)
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)

        logger.error("PDF generation failed: {}", ex.message, ex)
        return problemDetail
    }

    /**
     * Handle PDF generation timeout (>10 seconds)
     */
    @ExceptionHandler(PdfGenerationTimeoutException::class)
    fun handlePdfGenerationTimeoutException(
        ex: PdfGenerationTimeoutException,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val localizedMessage = getLocalizedMessage(exchange, MSG_PDF_TIMEOUT)

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.GATEWAY_TIMEOUT,
            "PDF generation took too long. Please try again with simpler content.",
        )
        problemDetail.title = "PDF Generation Timeout"
        problemDetail.type = URI.create("$ERROR_PAGE/resume/pdf-timeout")
        problemDetail.setProperty(ERROR_CATEGORY, "PDF_TIMEOUT")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE, MSG_PDF_TIMEOUT)
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)

        logger.warn("PDF generation timeout: {}", ex.message)
        return problemDetail
    }

    /**
     * Handle LaTeX injection attempts
     */
    @ExceptionHandler(LaTeXInjectionException::class)
    fun handleLaTeXInjectionException(
        ex: LaTeXInjectionException,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val localizedMessage = getLocalizedMessage(exchange, MSG_MALICIOUS_CONTENT)

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Content contains potentially unsafe characters.",
        )
        problemDetail.title = "Malicious Content Detected"
        problemDetail.type = URI.create("$ERROR_PAGE/resume/malicious-content")
        problemDetail.setProperty(ERROR_CATEGORY, "SECURITY_VIOLATION")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE, MSG_MALICIOUS_CONTENT)
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)

        logger.error("LaTeX injection attempt detected: {}", ex.message)
        return problemDetail
    }

    /**
     * Catch-all for unexpected errors
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val localizedMessage = getLocalizedMessage(exchange, MSG_INTERNAL_ERROR)

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please try again later.",
        )
        problemDetail.title = "Internal Server Error"
        problemDetail.type = URI.create("$ERROR_PAGE/resume/internal-error")
        problemDetail.setProperty(ERROR_CATEGORY, "INTERNAL_ERROR")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE, MSG_INTERNAL_ERROR)
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)

        logger.error("Unexpected error: {}", ex.message, ex)
        return problemDetail
    }

    companion object {
        private const val ERROR_PAGE = "https://cvix.com/errors"
        private const val TIMESTAMP = "timestamp"
        private const val ERROR_CATEGORY = "errorCategory"
        private const val MESSAGE = "message"
        private const val LOCALIZED_MESSAGE = "localizedMessage"
        private const val TRACE_ID = "traceId"

        // Message keys
        private const val MSG_VALIDATION_ERROR = "error.validation_error"
        private const val MSG_INVALID_DATA = "resume.error.invalid_data"
        private const val MSG_TEMPLATE_RENDERING = "resume.error.template_rendering"
        private const val MSG_PDF_GENERATION = "resume.error.pdf_generation"
        private const val MSG_PDF_TIMEOUT = "resume.error.pdf_timeout"
        private const val MSG_MALICIOUS_CONTENT = "resume.error.malicious_content"
        private const val MSG_INTERNAL_ERROR = "error.internal_server_error"
    }
}
