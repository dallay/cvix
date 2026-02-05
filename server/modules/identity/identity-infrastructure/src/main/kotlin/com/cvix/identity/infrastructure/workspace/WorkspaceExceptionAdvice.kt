package com.cvix.identity.infrastructure.workspace

import com.cvix.controllers.DEFAULT_PRECEDENCE
import com.cvix.controllers.ERROR_CATEGORY
import com.cvix.controllers.ERROR_PAGE
import com.cvix.controllers.LOCALIZED_MESSAGE
import com.cvix.controllers.MESSAGE_KEY
import com.cvix.controllers.MSG_AUTHORIZATION_FAILED
import com.cvix.controllers.TIMESTAMP
import com.cvix.controllers.TRACE_ID
import com.cvix.identity.domain.workspace.WorkspaceAuthorizationException
import com.cvix.identity.infrastructure.authentication.cookie.AuthCookieBuilder
import java.net.URI
import java.time.Instant
import java.util.Locale
import org.springframework.context.MessageSource
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.server.ServerWebExchange

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - DEFAULT_PRECEDENCE)
class WorkspaceExceptionAdvice(
    private val messageSource: MessageSource
) {

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(WorkspaceAuthorizationException::class)
    fun handleWorkspaceAuthorizationException(
        e: WorkspaceAuthorizationException,
        response: ServerHttpResponse,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val localizedMessage = getLocalizedMessage(exchange, MSG_AUTHORIZATION_FAILED)
        val problemDetail = com.cvix.controllers.createProblemDetail(
            status = HttpStatus.FORBIDDEN,
            title = "Workspace access forbidden",
            detail = e.message,
            typeSuffix = "workspace-authorization-failed",
            errorCategory = "AUTHORIZATION",
            exchange = exchange,
            messageKey = MSG_AUTHORIZATION_FAILED,
            localizedMessage = localizedMessage,
        )
        AuthCookieBuilder.clearCookies(response)
        return problemDetail
    }

    private fun getLocalizedMessage(exchange: ServerWebExchange, messageKey: String): String {
        val locale = exchange.localeContext.locale ?: Locale.getDefault()
        return messageSource.getMessage(messageKey, null, locale) ?: messageKey
    }

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
