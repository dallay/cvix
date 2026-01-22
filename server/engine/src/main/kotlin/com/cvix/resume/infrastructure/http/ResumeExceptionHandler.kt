package com.cvix.resume.infrastructure.http

import com.cvix.controllers.DEFAULT_INVALID_INPUT
import com.cvix.controllers.MESSAGE_KEY
import com.cvix.resume.domain.exception.InvalidResumeDataException
import com.cvix.resume.domain.exception.LaTeXInjectionException
import com.cvix.resume.domain.exception.PdfGenerationException
import com.cvix.resume.domain.exception.PdfGenerationTimeoutException
import com.cvix.resume.domain.exception.TemplateRenderingException
import java.net.URI
import java.time.Instant
import java.util.Locale
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException
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
        // Use overload that accepts a default message to avoid NoSuchMessageException
        return messageSource.getMessage(messageKey, null, messageKey, locale)
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

        val problemDetail = com.cvix.controllers.createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = "Validation Error",
            detail = "Request validation failed. Please check field and global errors.",
            typeSuffix = "resume/validation-error",
            errorCategory = "VALIDATION",
            exchange = exchange,
            messageKey = MSG_VALIDATION_ERROR,
            localizedMessage = localizedMessage,
            additionalProperties = mapOf("fieldErrors" to fieldErrors),
            includeInstance = false,
        )
        problemDetail.setProperty(MESSAGE_KEY, MSG_VALIDATION_ERROR)
        if (globalErrors.isNotEmpty()) {
            problemDetail.setProperty("errors", globalErrors)
        }

        logger.warn(
            "Validation error: {} fields failed, {} global errors",
            fieldErrors.size,
            globalErrors.size,
        )
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

        val problemDetail = com.cvix.controllers.createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = "Invalid Resume Data",
            detail = ex.message ?: "Resume data validation failed",
            typeSuffix = "resume/invalid-data",
            errorCategory = "INVALID_RESUME_DATA",
            exchange = exchange,
            messageKey = MSG_INVALID_DATA,
            localizedMessage = localizedMessage,
            includeInstance = false,
        )
        problemDetail.setProperty(MESSAGE_KEY, MSG_INVALID_DATA)

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

        val problemDetail = com.cvix.controllers.createProblemDetail(
            status = HttpStatus.UNPROCESSABLE_ENTITY,
            title = "Template Rendering Error",
            detail = "Failed to render resume template. Please check your data.",
            typeSuffix = "resume/template-error",
            errorCategory = "TEMPLATE_RENDERING",
            exchange = exchange,
            messageKey = MSG_TEMPLATE_RENDERING,
            localizedMessage = localizedMessage,
            includeInstance = false,
        )
        problemDetail.setProperty(MESSAGE_KEY, MSG_TEMPLATE_RENDERING)

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

        val problemDetail = com.cvix.controllers.createProblemDetail(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            title = "PDF Generation Error",
            detail = "Failed to generate PDF. Please try again later.",
            typeSuffix = "resume/pdf-generation-error",
            errorCategory = "PDF_GENERATION",
            exchange = exchange,
            messageKey = MSG_PDF_GENERATION,
            localizedMessage = localizedMessage,
            includeInstance = false,
        )
        problemDetail.setProperty(MESSAGE_KEY, MSG_PDF_GENERATION)

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

        val problemDetail = com.cvix.controllers.createProblemDetail(
            status = HttpStatus.GATEWAY_TIMEOUT,
            title = "PDF Generation Timeout",
            detail = "PDF generation took too long. Please try again with simpler content.",
            typeSuffix = "resume/pdf-timeout",
            errorCategory = "PDF_TIMEOUT",
            exchange = exchange,
            messageKey = MSG_PDF_TIMEOUT,
            localizedMessage = localizedMessage,
            includeInstance = false,
        )
        problemDetail.setProperty(MESSAGE_KEY, MSG_PDF_TIMEOUT)

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

        val problemDetail = com.cvix.controllers.createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = "Malicious Content Detected",
            detail = "Content contains potentially unsafe characters.",
            typeSuffix = "resume/malicious-content",
            errorCategory = "SECURITY_VIOLATION",
            exchange = exchange,
            messageKey = MSG_MALICIOUS_CONTENT,
            localizedMessage = localizedMessage,
            includeInstance = false,
        )
        problemDetail.setProperty(MESSAGE_KEY, MSG_MALICIOUS_CONTENT)

        logger.error("LaTeX injection attempt detected: {}", ex.message)
        return problemDetail
    }

    /**
     * Handle malformed or unreadable HTTP request bodies (JSON decoding errors, missing non-nullable fields)
     */
    @ExceptionHandler(org.springframework.web.server.ServerWebInputException::class)
    fun handleServerWebInputException(
        ex: org.springframework.web.server.ServerWebInputException,
        exchange: ServerWebExchange,
    ): ResponseEntity<ProblemDetail> {
        val localizedMessage = getLocalizedMessage(exchange, MSG_VALIDATION_ERROR)

        val problemDetail = com.cvix.controllers.createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = DEFAULT_INVALID_INPUT,
            detail = "Failed to read HTTP message: ${ex.message}",
            typeSuffix = "resume/bad-request",
            errorCategory = "INVALID_INPUT",
            exchange = exchange,
            messageKey = MSG_VALIDATION_ERROR,
            localizedMessage = localizedMessage,
            includeInstance = false,
        )
        problemDetail.setProperty(MESSAGE_KEY, MSG_VALIDATION_ERROR)

        logger.warn("Failed to read HTTP message: {}", ex.message)
        logger.warn("ProblemDetail title before return: {}", problemDetail.title)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail)
    }

    /**
     * Handle response status exceptions (e.g., 404 Not Found, 401 Unauthorized)
     */
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(
        ex: ResponseStatusException,
        exchange: ServerWebExchange,
    ): ResponseEntity<ProblemDetail> {
        // Map the exception's statusCode (HttpStatusCode) to an HttpStatus when possible
        val statusCode = ex.statusCode ?: HttpStatus.INTERNAL_SERVER_ERROR
        val numericStatus = statusCode.value()
        val httpStatus = HttpStatus.resolve(numericStatus) ?: HttpStatus.INTERNAL_SERVER_ERROR

        val detail = ex.reason ?: ex.message

        val problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, detail)
        problemDetail.title = ex.reason ?: httpStatus.reasonPhrase
        problemDetail.type = URI.create("$ERROR_PAGE/resume/${httpStatus.value()}-error")
        problemDetail.setProperty(ERROR_CATEGORY, "HTTP_${httpStatus.value()}")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE_KEY, "error.http_${httpStatus.value()}")
        problemDetail.setProperty(LOCALIZED_MESSAGE, getLocalizedMessage(exchange, "error.http_${httpStatus.value()}"))
        problemDetail.setProperty(TRACE_ID, exchange.request.id)

        logger.warn("HTTP error handled: {} {}", httpStatus, ex.reason)
        return ResponseEntity.status(httpStatus).body(problemDetail)
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

        val problemDetail = com.cvix.controllers.createProblemDetail(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            title = "Internal Server Error",
            detail = "An unexpected error occurred. Please try again later.",
            typeSuffix = "resume/internal-error",
            errorCategory = "INTERNAL_ERROR",
            exchange = exchange,
            messageKey = MSG_INTERNAL_ERROR,
            localizedMessage = localizedMessage,
            includeInstance = false,
        )
        problemDetail.setProperty(MESSAGE_KEY, MSG_INTERNAL_ERROR)

        logger.error("Unexpected error: {}", ex.message, ex)
        return problemDetail
    }

    companion object {
        private const val ERROR_PAGE = "https://profiletailors.com/errors"
        private const val TIMESTAMP = "timestamp"
        private const val ERROR_CATEGORY = "errorCategory"
        // message key is defined in shared ExceptionHandlerConstants as MESSAGE_KEY
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
