package com.loomify.resume.infrastructure.web

import com.loomify.resume.domain.exception.InvalidResumeDataException
import com.loomify.resume.domain.exception.LaTeXInjectionException
import com.loomify.resume.domain.exception.PdfGenerationException
import com.loomify.resume.domain.exception.PdfGenerationTimeoutException
import com.loomify.resume.domain.exception.TemplateRenderingException
import java.net.URI
import java.time.Instant
import org.slf4j.LoggerFactory
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
@RestControllerAdvice("com.loomify.resume")
class ResumeExceptionHandler {

    private val logger = LoggerFactory.getLogger(ResumeExceptionHandler::class.java)

    /**
     * Handle validation errors from Spring Validation (@Valid)
     */
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationException(
        ex: WebExchangeBindException,
        @Suppress("UNUSED_PARAMETER") exchange: ServerWebExchange
    ): ProblemDetail {
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
        problemDetail.setProperty("fieldErrors", fieldErrors)
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
        @Suppress("UNUSED_PARAMETER") exchange: ServerWebExchange
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.message ?: "Resume data validation failed",
        )
        problemDetail.title = "Invalid Resume Data"
        problemDetail.type = URI.create("$ERROR_PAGE/resume/invalid-data")
        problemDetail.setProperty(ERROR_CATEGORY, "INVALID_RESUME_DATA")
        problemDetail.setProperty(TIMESTAMP, Instant.now())

        logger.warn("Invalid resume data: {}", ex.message)
        return problemDetail
    }

    /**
     * Handle template rendering errors (LaTeX syntax, missing data)
     */
    @ExceptionHandler(TemplateRenderingException::class)
    fun handleTemplateRenderingException(
        ex: TemplateRenderingException,
        @Suppress("UNUSED_PARAMETER") exchange: ServerWebExchange
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Failed to render resume template. Please check your data.",
        )
        problemDetail.title = "Template Rendering Error"
        problemDetail.type = URI.create("$ERROR_PAGE/resume/template-error")
        problemDetail.setProperty(ERROR_CATEGORY, "TEMPLATE_RENDERING")
        problemDetail.setProperty(TIMESTAMP, Instant.now())

        logger.error("Template rendering failed: {}", ex.message, ex)
        return problemDetail
    }

    /**
     * Handle PDF generation failures (Docker, LaTeX compilation)
     */
    @ExceptionHandler(PdfGenerationException::class)
    fun handlePdfGenerationException(
        ex: PdfGenerationException,
        @Suppress("UNUSED_PARAMETER") exchange: ServerWebExchange
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Failed to generate PDF. Please try again later.",
        )
        problemDetail.title = "PDF Generation Error"
        problemDetail.type = URI.create("$ERROR_PAGE/resume/pdf-generation-error")
        problemDetail.setProperty(ERROR_CATEGORY, "PDF_GENERATION")
        problemDetail.setProperty(TIMESTAMP, Instant.now())

        logger.error("PDF generation failed: {}", ex.message, ex)
        return problemDetail
    }

    /**
     * Handle PDF generation timeout (>10 seconds)
     */
    @ExceptionHandler(PdfGenerationTimeoutException::class)
    fun handlePdfGenerationTimeoutException(
        ex: PdfGenerationTimeoutException,
        @Suppress("UNUSED_PARAMETER") exchange: ServerWebExchange
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.GATEWAY_TIMEOUT,
            "PDF generation took too long. Please try again with simpler content.",
        )
        problemDetail.title = "PDF Generation Timeout"
        problemDetail.type = URI.create("$ERROR_PAGE/resume/pdf-timeout")
        problemDetail.setProperty(ERROR_CATEGORY, "PDF_TIMEOUT")
        problemDetail.setProperty(TIMESTAMP, Instant.now())

        logger.warn("PDF generation timeout: {}", ex.message)
        return problemDetail
    }

    /**
     * Handle LaTeX injection attempts
     */
    @ExceptionHandler(LaTeXInjectionException::class)
    fun handleLaTeXInjectionException(
        ex: LaTeXInjectionException,
        @Suppress("UNUSED_PARAMETER") exchange: ServerWebExchange
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Content contains potentially unsafe characters.",
        )
        problemDetail.title = "Malicious Content Detected"
        problemDetail.type = URI.create("$ERROR_PAGE/resume/malicious-content")
        problemDetail.setProperty(ERROR_CATEGORY, "SECURITY_VIOLATION")
        problemDetail.setProperty(TIMESTAMP, Instant.now())

        logger.error("LaTeX injection attempt detected: {}", ex.message)
        return problemDetail
    }

    /**
     * Catch-all for unexpected errors
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        @Suppress("UNUSED_PARAMETER") exchange: ServerWebExchange
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please try again later.",
        )
        problemDetail.title = "Internal Server Error"
        problemDetail.type = URI.create("$ERROR_PAGE/resume/internal-error")
        problemDetail.setProperty(ERROR_CATEGORY, "INTERNAL_ERROR")
        problemDetail.setProperty(TIMESTAMP, Instant.now())

        logger.error("Unexpected error: {}", ex.message, ex)
        return problemDetail
    }

    companion object {
        private const val ERROR_PAGE = "https://loomify.com/errors"
        private const val TIMESTAMP = "timestamp"
        private const val ERROR_CATEGORY = "errorCategory"
    }
}
