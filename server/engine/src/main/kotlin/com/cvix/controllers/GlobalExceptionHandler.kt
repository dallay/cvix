package com.cvix.controllers

import com.cvix.authentication.domain.UserAuthenticationException
import com.cvix.authentication.domain.UserRefreshTokenException
import com.cvix.authentication.domain.error.LogoutFailedException
import com.cvix.authentication.domain.error.MissingCookieException
import com.cvix.authentication.infrastructure.cookie.AuthCookieBuilder
import com.cvix.common.domain.error.BusinessRuleValidationException
import com.cvix.common.domain.error.EntityNotFoundException
import com.cvix.waitlist.domain.EmailAlreadyExistsException
import com.cvix.workspace.domain.WorkspaceAuthorizationException
import java.net.URI
import java.time.Instant
import java.util.Locale
import org.springframework.context.MessageSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

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
    )
    fun handleUserAuthenticationException(
        e: Exception,
        response: ServerHttpResponse,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val localizedMessage = getLocalizedMessage(exchange, MSG_AUTHENTICATION_FAILED)

        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message)
        problemDetail.title = "User authentication failed"
        problemDetail.type = URI.create("$ERROR_PAGE/user-authentication-failed")
        problemDetail.setProperty("errorCategory", "AUTHENTICATION")
        problemDetail.setProperty("timestamp", Instant.now())
        problemDetail.setProperty(MESSAGE_KEY, MSG_AUTHENTICATION_FAILED)
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)
        AuthCookieBuilder.clearCookies(response)
        return problemDetail
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(WorkspaceAuthorizationException::class)
    fun handleWorkspaceAuthorizationException(
        e: WorkspaceAuthorizationException,
        response: ServerHttpResponse,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val localizedMessage = getLocalizedMessage(exchange, MSG_AUTHORIZATION_FAILED)

        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.message)
        problemDetail.title = "Workspace access forbidden"
        problemDetail.type = URI.create("$ERROR_PAGE/workspace-authorization-failed")
        problemDetail.setProperty("errorCategory", "AUTHORIZATION")
        problemDetail.setProperty("timestamp", Instant.now())
        problemDetail.setProperty(MESSAGE_KEY, MSG_AUTHORIZATION_FAILED)
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
        val localizedMessage = getLocalizedMessage(exchange, MSG_ENTITY_NOT_FOUND)

        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message ?: ENTITY_NOT_FOUND)
        problemDetail.title = ENTITY_NOT_FOUND
        problemDetail.type = URI.create("$ERROR_PAGE/entity-not-found")
        problemDetail.setProperty(ERROR_CATEGORY, "NOT_FOUND")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE_KEY, MSG_ENTITY_NOT_FOUND)
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)
        return problemDetail
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExistsException(
        @Suppress("UNUSED_PARAMETER") ex: EmailAlreadyExistsException,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            "This email is already on the waitlist",
        )
        problemDetail.title = "Conflict"
        problemDetail.type = URI.create("$ERROR_PAGE/waitlist/email-already-exists")
        problemDetail.instance = URI.create(exchange.request.path.toString())
        problemDetail.setProperty(ERROR_CATEGORY, "EMAIL_ALREADY_EXISTS")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
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
        val locale = exchange.localeContext.locale ?: java.util.Locale.getDefault()
        val localizedTitle = messageSource.getMessage(TITLE_INVALID_INPUT, emptyArray(), "Invalid Input", locale)
        val detail = e.message ?: messageSource.getMessage(MSG_BAD_REQUEST, emptyArray(), "Bad request", locale)
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.title = localizedTitle
        problemDetail.detail = detail
        problemDetail.type = URI.create("$ERROR_PAGE/bad-request")
        problemDetail.setProperty(ERROR_CATEGORY, "BAD_REQUEST")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE_KEY, MSG_BAD_REQUEST)
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedTitle)
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
        val localizedMessage = getLocalizedMessage(exchange, MSG_MISSING_COOKIE)

        response.statusCode = HttpStatus.BAD_REQUEST
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)
        problemDetail.title = "Missing cookie"
        problemDetail.type = URI.create("$ERROR_PAGE/missing-cookie")
        problemDetail.setProperty(ERROR_CATEGORY, "MISSING_COOKIE")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE_KEY, MSG_MISSING_COOKIE)
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
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange,
    ): Mono<ResponseEntity<Any>> {
        val locale = exchange.localeContext.locale ?: java.util.Locale.getDefault()
        val title = messageSource.getMessage(TITLE_VALIDATION_ERROR, emptyArray(), "Validation Failed", locale)
        val detail = messageSource.getMessage(
            MSG_VALIDATION_ERROR,
            emptyArray(),
            "Request validation failed. Please check the provided data.",
            locale,
        )
        val fieldErrors = ex.bindingResult.fieldErrors.map { fieldError ->
            mapOf(
                "field" to fieldError.field,
                "message" to (fieldError.defaultMessage ?: "Invalid value"),
                "rejectedValue" to fieldError.rejectedValue,
            )
        }
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.title = title
        problemDetail.detail = detail
        problemDetail.type = URI.create("$ERROR_PAGE/validation-error")
        problemDetail.setProperty(ERROR_CATEGORY, "VALIDATION")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE_KEY, MSG_VALIDATION_ERROR)
        problemDetail.setProperty(LOCALIZED_MESSAGE, title)
        problemDetail.setProperty("errors", fieldErrors)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail))
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception, exchange: ServerWebExchange): ProblemDetail {
        val localizedMessage = getLocalizedMessage(exchange, MSG_INTERNAL_SERVER_ERROR)

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            e.message ?: "Internal server error",
        )
        problemDetail.title = "Internal server error"
        problemDetail.type = URI.create("$ERROR_PAGE/internal-server-error")
        problemDetail.setProperty(ERROR_CATEGORY, "INTERNAL_SERVER_ERROR")
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(MESSAGE_KEY, MSG_INTERNAL_SERVER_ERROR)
        problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        problemDetail.setProperty(TRACE_ID, exchange.request.id)
        return problemDetail
    }

    private fun getLocalizedMessage(exchange: ServerWebExchange, messageKey: String): String {
        val locale = exchange.localeContext.locale ?: Locale.getDefault()
        return messageSource.getMessage(messageKey, null, locale)
    }
}
