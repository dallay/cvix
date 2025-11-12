package com.loomify.engine.controllers

import com.loomify.common.domain.error.BusinessRuleValidationException
import com.loomify.common.domain.error.EntityNotFoundException
import com.loomify.engine.authentication.domain.UserAuthenticationException
import com.loomify.engine.authentication.domain.UserRefreshTokenException
import com.loomify.engine.authentication.domain.error.LogoutFailedException
import com.loomify.engine.authentication.domain.error.MissingCookieException
import com.loomify.engine.authentication.infrastructure.cookie.AuthCookieBuilder
import com.loomify.engine.workspace.domain.WorkspaceAuthorizationException
import java.net.URI
import java.time.Instant
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

private const val ERROR_PAGE = "https://loomify.com/errors"

private const val TIMESTAMP = "timestamp"

private const val ENTITY_NOT_FOUND = "Entity not found"

private const val ERROR_CATEGORY = "errorCategory"

private const val LOCALIZED_MESSAGE = "localizedMessage"

private const val MESSAGE = "message"

private const val TRACE_ID = "traceId"

/**
 * This class provides a global exception handling mechanism for the application.
 *
 * It extends the [ResponseEntityExceptionHandler] class to handle exceptions and return appropriate responses.
 *
 * @created 4/8/23
 */
@RestControllerAdvice
class GlobalExceptionHandler(
    private val messageSource: MessageSource
) : ResponseEntityExceptionHandler() {
    /**
     * Handles the [UserAuthenticationException] by creating a ProblemDetail object with the appropriate status,
     * detail and properties.
     *
     * @param e The UserAuthenticationException that was thrown.
     * @return The ProblemDetail object representing the exception.
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(
        UserAuthenticationException::class, UserRefreshTokenException::class,
        WorkspaceAuthorizationException::class,
    )
    fun handleUserAuthenticationException(
        e: Exception,
        response: ServerHttpResponse,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val locale = LocaleContextHolder.getLocale()
        val localizedMessage = messageSource.getMessage("error.authentication_failed", null, locale)
        
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message)
        problemDetail.title = "User authentication failed"
        problemDetail.type = URI.create("$ERROR_PAGE/user-authentication-failed")
        problemDetail.setProperty("errorCategory", "AUTHENTICATION")
        problemDetail.setProperty("timestamp", Instant.now())
        problemDetail.setProperty(MESSAGE, "error.authentication_failed")
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)
        AuthCookieBuilder.clearCookies(response)
        return problemDetail
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(
        EntityNotFoundException::class,
    )
    fun handleEntityNotFound(e: Exception, exchange: ServerWebExchange): ProblemDetail {
        val locale = LocaleContextHolder.getLocale()
        val localizedMessage = messageSource.getMessage("error.entity_not_found", null, locale)
        
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message ?: ENTITY_NOT_FOUND)
        problemDetail.title = ENTITY_NOT_FOUND
        problemDetail.type = URI.create("$ERROR_PAGE/entity-not-found")
        problemDetail.setProperty(ERROR_CATEGORY, "NOT_FOUND")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE, "error.entity_not_found")
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)
        return problemDetail
    }

    /**
     * Handles IllegalArgumentExceptions by creating a ProblemDetail object with the appropriate status,
     * detail and properties.
     * @param e The IllegalArgumentException that was thrown.
     * @return The ProblemDetail object representing the exception.
     * @see ProblemDetail
     * @see HttpStatus
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(
        IllegalArgumentException::class,
        BusinessRuleValidationException::class,
        LogoutFailedException::class,
    )
    fun handleIllegalArgumentException(e: Exception, exchange: ServerWebExchange): ProblemDetail {
        val locale = LocaleContextHolder.getLocale()
        val localizedMessage = messageSource.getMessage("error.bad_request", null, locale)
        
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message ?: "Bad request")
        problemDetail.title = "Bad request"
        problemDetail.type = URI.create("$ERROR_PAGE/bad-request")
        problemDetail.setProperty(ERROR_CATEGORY, "BAD_REQUEST")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE, "error.bad_request")
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)
        return problemDetail
    }
    /**
     * Exception handler for missing cookies.
     *
     * @param e The MissingCookieException.
     * @param response The HTTP response where the error status will be set.
     */
    @ExceptionHandler(MissingCookieException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingCookieException(
        e: MissingCookieException,
        response: ServerHttpResponse,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val locale = LocaleContextHolder.getLocale()
        val localizedMessage = messageSource.getMessage("error.missing_cookie", null, locale)
        
        response.statusCode = HttpStatus.BAD_REQUEST
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)
        problemDetail.title = "Missing cookie"
        problemDetail.type = URI.create("$ERROR_PAGE/missing-cookie")
        problemDetail.setProperty(ERROR_CATEGORY, "MISSING_COOKIE")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE, "error.missing_cookie")
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)
        return problemDetail
    }

    /**
     * Handles validation exceptions from @Valid/@Validated annotations.
     * Overrides the base class method to return field-specific error messages.
     *
     * @param ex The WebExchangeBindException containing validation errors.
     * @param headers The HTTP headers.
     * @param status The HTTP status.
     * @param exchange The server web exchange.
     * @return A Mono with a ResponseEntity containing the ProblemDetail with field errors.
     */
    override fun handleWebExchangeBindException(
        ex: WebExchangeBindException,
        headers: org.springframework.http.HttpHeaders,
        status: org.springframework.http.HttpStatusCode,
        exchange: org.springframework.web.server.ServerWebExchange,
    ): Mono<org.springframework.http.ResponseEntity<Any>> {
        val locale = LocaleContextHolder.getLocale()
        val localizedMessage = messageSource.getMessage("error.validation_error", null, locale)
        
        val fieldErrors = ex.bindingResult.fieldErrors.associate { error ->
            error.field to (error.defaultMessage ?: "Invalid value")
        }
        val globalErrors = ex.bindingResult.globalErrors.map { error ->
            error.defaultMessage ?: "Validation failed"
        }

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            globalErrors.firstOrNull() ?: "Validation failed",
        )
        problemDetail.title = "Validation Error"
        problemDetail.type = URI.create("$ERROR_PAGE/validation-error")
        problemDetail.setProperty(ERROR_CATEGORY, "VALIDATION")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE, "error.validation_error")
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty("fieldErrors", fieldErrors)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)
        if (globalErrors.isNotEmpty()) {
            problemDetail.setProperty("errors", globalErrors)
        }
        return Mono.just(org.springframework.http.ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail))
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception, exchange: ServerWebExchange): ProblemDetail {
        val locale = LocaleContextHolder.getLocale()
        val localizedMessage = messageSource.getMessage("error.internal_server_error", null, locale)
        
        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            e.message ?: "Internal server error",
        )
        problemDetail.title = "Internal server error"
        problemDetail.type = URI.create("$ERROR_PAGE/internal-server-error")
        problemDetail.setProperty(ERROR_CATEGORY, "INTERNAL_SERVER_ERROR")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE, "error.internal_server_error")
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)
        return problemDetail
    }
}
