package com.cvix.controllers

import java.time.Instant
import org.springframework.context.MessageSource
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebExchange

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - DEFAULT_PRECEDENCE)
class GlobalValidationAdviceHandler(
    private val messageSource: MessageSource
) {

    /**
     * Handles WebExchangeBindException, typically caused by validation errors.
     *
     * @return ProblemDetail containing HTTP 400 status, error message, and field-specific validation errors.
     */
    @ExceptionHandler(WebExchangeBindException::class)
    fun WebExchangeBindException.handleValidationException(exchange: ServerWebExchange): ProblemDetail {
        val locale = exchange.localeContext.locale
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = messageSource.getMessage(
            TITLE_VALIDATION_ERROR,
            emptyArray(),
            "Validation Failed",
            locale,
        )
        detail.detail = messageSource.getMessage(
            MSG_VALIDATION_ERROR,
            emptyArray(),
            "Request validation failed. Please check the provided data.",
            locale,
        )
        detail.setProperty(MESSAGE_KEY, MSG_VALIDATION_ERROR)
        val fieldErrors = this.bindingResult.fieldErrors.map { fieldError ->
            mapOf(
                "field" to fieldError.field,
                "message" to (fieldError.defaultMessage ?: "Invalid value"),
                "rejectedValue" to fieldError.rejectedValue,
            )
        }
        detail.setProperty("errors", fieldErrors)
        detail.setProperty(TIMESTAMP, Instant.now().toString())
        return detail
    }

    /**
     * Handles IllegalArgumentException, typically caused by invalid input.
     *
     * @return ProblemDetail containing HTTP 400 status and error message.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun IllegalArgumentException.handleIllegalArgument(exchange: ServerWebExchange): ProblemDetail {
        val locale = exchange.localeContext.locale
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title =
            messageSource.getMessage(TITLE_INVALID_INPUT, emptyArray(), "Invalid Input", locale)
        detail.detail = this.message ?: messageSource.getMessage(
            MSG_BAD_REQUEST,
            emptyArray(),
            "Bad request",
            locale,
        )
        detail.setProperty(MESSAGE_KEY, MSG_BAD_REQUEST)
        detail.setProperty(TIMESTAMP, Instant.now().toString())
        return detail
    }
}
