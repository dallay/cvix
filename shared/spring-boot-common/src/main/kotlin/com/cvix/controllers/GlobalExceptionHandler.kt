package com.cvix.controllers

import com.cvix.common.domain.error.BusinessRuleValidationException
import com.cvix.common.domain.error.EntityNotFoundException
import java.net.URI
import java.time.Instant
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono

/**
 * This class provides a global exception handling mechanism for the application.
 *
 * It extends the [ResponseEntityExceptionHandler] class to handle exceptions and return appropriate responses.
 *
 * @created 4/8/23
 */
@RestControllerAdvice
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.LOWEST_PRECEDENCE)
class GlobalExceptionHandler(
    private val messageSource: MessageSource
) : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(
        EntityNotFoundException::class,
    )
    fun handleEntityNotFound(e: Exception, exchange: ServerWebExchange): ProblemDetail {
        val localizedMessage = getLocalizedMessage(exchange, MSG_ENTITY_NOT_FOUND)
        return createProblemDetail(
            status = HttpStatus.NOT_FOUND,
            title = ENTITY_NOT_FOUND,
            detail = e.message ?: ENTITY_NOT_FOUND,
            typeSuffix = "entity-not-found",
            errorCategory = "NOT_FOUND",
            exchange = exchange,
            messageKey = MSG_ENTITY_NOT_FOUND,
            localizedMessage = localizedMessage,
            includeInstance = true,
        )
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
    )
    fun handleIllegalArgumentException(e: Exception, exchange: ServerWebExchange): ProblemDetail {
        val locale = exchange.localeContext.locale ?: Locale.getDefault()
        val defaultDetail = "Bad request"
        val localizedTitle: String = messageSource.getMessage(
            TITLE_INVALID_INPUT,
            emptyArray(),
            DEFAULT_INVALID_INPUT,
            locale,
        ) ?: DEFAULT_INVALID_INPUT
        val detail: String = e.message ?: messageSource.getMessage(
            MSG_BAD_REQUEST,
            emptyArray(),
            defaultDetail,
            locale,
        ) ?: defaultDetail
        return createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = localizedTitle,
            detail = detail,
            typeSuffix = "bad-request",
            errorCategory = "BAD_REQUEST",
            exchange = exchange,
            messageKey = MSG_BAD_REQUEST,
            localizedMessage = localizedTitle,
        )
    }

    /**
     * Handles JSON deserialization exceptions from Spring WebFlux.
     * This occurs when Jackson cannot parse the request body into the expected DTO.
     *
     * @param ex The DecodingException containing the parsing error details.
     * @param exchange The server web exchange.
     * @return A ProblemDetail with detailed information about the deserialization failure.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DecodingException::class)
    fun handleDecodingException(
        ex: DecodingException,
        exchange: ServerWebExchange
    ): ProblemDetail {
        logRequestException("JSON deserialization failed", exchange, ex)

        val locale = exchange.localeContext.locale ?: Locale.getDefault()
        val title: String = messageSource.getMessage(
            "error.json.parsing.title",
            emptyArray(),
            "Invalid Request Body",
            locale,
        ) ?: "Invalid Request Body"

        // Extract the root cause message which usually contains the field name
        val rootCause = ex.mostSpecificCause
        val detailMessage: String = rootCause.message ?: ex.message ?: "Failed to parse request body"

        return createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = title,
            detail = detailMessage,
            typeSuffix = "json-parsing-error",
            errorCategory = "JSON_PARSING",
            exchange = exchange,
            additionalProperties = mapOf("rootCause" to (rootCause::class.simpleName ?: "Unknown")),
            includeInstance = true,
        )
    }

    /**
     * Overrides the parent handler for ServerWebInputException to provide detailed error logging.
     * This handles deserialization errors not caught by handleDecodingException.
     *
     * @param ex The ServerWebInputException.
     * @param headers The HTTP headers.
     * @param status The HTTP status.
     * @param exchange The server web exchange.
     * @return A Mono with a ResponseEntity containing the ProblemDetail.
     */
    override fun handleServerWebInputException(
        ex: ServerWebInputException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Any>> {
        logRequestException("Server web input exception", exchange, ex)

        val locale = exchange.localeContext.locale ?: Locale.getDefault()
        val title: String = messageSource.getMessage(
            "error.input.invalid.title",
            emptyArray(),
            DEFAULT_INVALID_INPUT,
            locale,
        ) ?: DEFAULT_INVALID_INPUT

        // Try to get a meaningful error message from the cause chain
        val rootCause = ex.mostSpecificCause
        val detailMessage: String = when {
            rootCause is DecodingException -> rootCause.mostSpecificCause.message
            rootCause.message != null -> rootCause.message
            else -> ex.reason
        } ?: "Invalid input data"

        val problemDetail = createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = title,
            detail = detailMessage,
            typeSuffix = "input-error",
            errorCategory = "INPUT_ERROR",
            exchange = exchange,
            additionalProperties = mapOf("rootCause" to (rootCause::class.simpleName ?: "Unknown")),
            includeInstance = true,
        )
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail))
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
        val locale = exchange.localeContext.locale ?: Locale.getDefault()
        val title: String = messageSource.getMessage(TITLE_VALIDATION_ERROR, emptyArray(), "Validation Failed", locale)
            ?: "Validation Failed"
        val detail: String = messageSource.getMessage(
            MSG_VALIDATION_ERROR,
            emptyArray(),
            "Request validation failed. Please check the provided data.",
            locale,
        ) ?: "Request validation failed. Please check the provided data."
        val fieldErrors = ex.bindingResult.fieldErrors.map { fieldError ->
            mapOf(
                "field" to fieldError.field,
                "message" to (fieldError.defaultMessage ?: "Invalid value"),
                "rejectedValue" to fieldError.rejectedValue,
            )
        }
        val problemDetail = createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = title,
            detail = detail,
            typeSuffix = "validation-error",
            errorCategory = "VALIDATION",
            exchange = exchange,
            messageKey = MSG_VALIDATION_ERROR,
            localizedMessage = title,
            additionalProperties = mapOf("errors" to fieldErrors),
        )
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail))
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception, exchange: ServerWebExchange): ProblemDetail {
        // Log full exception details server-side for debugging
        log.error("Unhandled exception: ${e.message}", e)

        val localizedMessage = getLocalizedMessage(exchange, MSG_INTERNAL_SERVER_ERROR)

        // Never expose exception details to clients (security risk)
        return createProblemDetail(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            title = "Internal server error",
            detail = "An internal server error occurred",
            typeSuffix = "internal-server-error",
            errorCategory = "INTERNAL_SERVER_ERROR",
            exchange = exchange,
            messageKey = MSG_INTERNAL_SERVER_ERROR,
            localizedMessage = localizedMessage,
        )
    }

    private fun getLocalizedMessage(exchange: ServerWebExchange, messageKey: String): String {
        val locale = exchange.localeContext.locale ?: Locale.getDefault()
        return try {
            messageSource.getMessage(messageKey, null, locale) ?: messageKey
        } catch (_: NoSuchMessageException) {
            messageKey
        }
    }

    /**
     * Logs request exceptions with consistent formatting.
     */
    private fun logRequestException(message: String, exchange: ServerWebExchange, ex: Exception) {
        log.error(
            "$message for request {} {}",
            exchange.request.method,
            exchange.request.path,
            ex,
        )
    }

    /**
     * Creates a ProblemDetail with common properties set.
     * Reduces code duplication across exception handlers.
     *
     * @param status HTTP status code
     * @param title Human-readable title
     * @param detail Detailed error message
     * @param typeSuffix URL path suffix for the problem type (e.g., "authentication-failed")
     * @param errorCategory Error category for client-side filtering/handling
     * @param exchange Server web exchange for extracting request information
     * @param messageKey Optional i18n message key
     * @param localizedMessage Optional localized message
     * @param additionalProperties Optional map of additional properties to set on the ProblemDetail
     * @param includeInstance If true, sets the instance property to the request path
     * @return Configured ProblemDetail instance
     */
    private fun createProblemDetail(
        status: HttpStatus,
        title: String,
        detail: String?,
        typeSuffix: String,
        errorCategory: String,
        exchange: ServerWebExchange,
        messageKey: String? = null,
        localizedMessage: String? = null,
        additionalProperties: Map<String, Any>? = null,
        includeInstance: Boolean = false,
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(status, detail ?: title)
        problemDetail.title = title
        problemDetail.type = URI.create("$ERROR_PAGE/$typeSuffix")
        problemDetail.setProperty(ERROR_CATEGORY, errorCategory)
        problemDetail.setProperty(TIMESTAMP, Instant.now())
        problemDetail.setProperty(TRACE_ID, exchange.request.id)

        if (includeInstance) {
            problemDetail.instance = URI.create(exchange.request.path.toString())
        }

        if (messageKey != null) {
            problemDetail.setProperty(MESSAGE_KEY, messageKey)
        }
        if (localizedMessage != null) {
            problemDetail.setProperty(LOCALIZED_MESSAGE, localizedMessage)
        }

        additionalProperties?.forEach { (key, value) ->
            problemDetail.setProperty(key, value)
        }

        return problemDetail
    }
}
