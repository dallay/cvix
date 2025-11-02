package com.loomify.resume.infrastructure.web

import com.loomify.resume.domain.exception.InvalidResumeDataException
import com.loomify.resume.domain.exception.LaTeXInjectionException
import com.loomify.resume.domain.exception.PdfGenerationException
import com.loomify.resume.domain.exception.PdfGenerationTimeoutException
import com.loomify.resume.domain.exception.TemplateRenderingException
import com.loomify.resume.infrastructure.web.dto.ErrorResponse
import com.loomify.resume.infrastructure.web.dto.FieldError
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Global exception handler for resume generation endpoints.
 * Translates domain exceptions to HTTP responses with standardized error format.
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
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        val fieldErrors = ex.fieldErrors.map { error ->
            FieldError(
                field = error.field,
                message = error.defaultMessage ?: "Invalid value",
                rejectedValue = error.rejectedValue,
            )
        }

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            code = "validation_error",
            message = "Request validation failed. Please check field errors.",
            path = exchange.request.path.value(),
            errors = fieldErrors,
        )

        logger.warn("Validation error: {} fields failed", fieldErrors.size)
        return Mono.just(ResponseEntity.badRequest().body(errorResponse))
    }

    /**
     * Handle invalid resume data (missing required sections, business rule violations)
     */
    @ExceptionHandler(InvalidResumeDataException::class)
    fun handleInvalidResumeDataException(
        ex: InvalidResumeDataException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            code = "invalid_resume_data",
            message = ex.message ?: "Resume data validation failed",
            path = exchange.request.path.value(),
        )

        logger.warn("Invalid resume data: {}", ex.message)
        return Mono.just(ResponseEntity.badRequest().body(errorResponse))
    }

    /**
     * Handle template rendering errors (LaTeX syntax, missing data)
     */
    @ExceptionHandler(TemplateRenderingException::class)
    fun handleTemplateRenderingException(
        ex: TemplateRenderingException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            code = "template_rendering_error",
            message = "Failed to render resume template. Please check your data.",
            path = exchange.request.path.value(),
        )

        logger.error("Template rendering failed: {}", ex.message, ex)
        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse))
    }

    /**
     * Handle PDF generation failures (Docker, LaTeX compilation)
     */
    @ExceptionHandler(PdfGenerationException::class)
    fun handlePdfGenerationException(
        ex: PdfGenerationException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            code = "pdf_generation_error",
            message = "Failed to generate PDF. Please try again later.",
            path = exchange.request.path.value(),
        )

        logger.error("PDF generation failed: {}", ex.message, ex)
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse))
    }

    /**
     * Handle PDF generation timeout (>10 seconds)
     */
    @ExceptionHandler(PdfGenerationTimeoutException::class)
    fun handlePdfGenerationTimeoutException(
        ex: PdfGenerationTimeoutException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.GATEWAY_TIMEOUT.value(),
            code = "pdf_generation_timeout",
            message = "PDF generation took too long. Please try again with simpler content.",
            path = exchange.request.path.value(),
        )

        logger.warn("PDF generation timeout: {}", ex.message)
        return Mono.just(ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(errorResponse))
    }

    /**
     * Handle LaTeX injection attempts
     */
    @ExceptionHandler(LaTeXInjectionException::class)
    fun handleLaTeXInjectionException(
        ex: LaTeXInjectionException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            code = "malicious_content",
            message = "Content contains potentially unsafe characters.",
            path = exchange.request.path.value(),
        )

        logger.error("LaTeX injection attempt detected: {}", ex.message)
        return Mono.just(ResponseEntity.badRequest().body(errorResponse))
    }

    /**
     * Catch-all for unexpected errors
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            code = "internal_error",
            message = "An unexpected error occurred. Please try again later.",
            path = exchange.request.path.value(),
        )

        logger.error("Unexpected error: {}", ex.message, ex)
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse))
    }
}
