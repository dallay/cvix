package com.cvix.waitlist.infrastructure

import com.cvix.controllers.DEFAULT_PRECEDENCE
import com.cvix.waitlist.domain.EmailAlreadyExistsException
import java.util.Locale
import org.springframework.context.MessageSource
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.server.ServerWebExchange

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - DEFAULT_PRECEDENCE)
class WaitlistExceptionAdvice(
    private val messageSource: MessageSource
) {

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExistsException(exchange: ServerWebExchange): ProblemDetail {
        val locale = exchange.localeContext.locale ?: Locale.getDefault()
        val localizedTitle: String = messageSource.getMessage(
            "error.email.already.exists.title",
            emptyArray(),
            "Conflict",
            locale,
        ) ?: "Conflict"
        val localizedDetail: String = messageSource.getMessage(
            "error.email.already.exists.detail",
            emptyArray(),
            "This email is already on the waitlist",
            locale,
        ) ?: "This email is already on the waitlist"

        return com.cvix.controllers.createProblemDetail(
            status = HttpStatus.CONFLICT,
            title = localizedTitle,
            detail = localizedDetail,
            typeSuffix = "waitlist/email-already-exists",
            errorCategory = "EMAIL_ALREADY_EXISTS",
            exchange = exchange,
            localizedMessage = localizedTitle,
            includeInstance = true,
        )
    }
}
