package com.cvix.identity.infrastructure.authentication

import com.cvix.controllers.DEFAULT_PRECEDENCE
import com.cvix.controllers.ERROR_CATEGORY
import com.cvix.controllers.ERROR_PAGE
import com.cvix.controllers.LOCALIZED_MESSAGE
import com.cvix.controllers.MESSAGE_KEY
import com.cvix.controllers.MSG_AUTHENTICATION_FAILED
import com.cvix.controllers.MSG_MISSING_COOKIE
import com.cvix.controllers.TIMESTAMP
import com.cvix.controllers.TRACE_ID
import com.cvix.controllers.createProblemDetail
import com.cvix.identity.domain.authentication.UserAuthenticationException
import com.cvix.identity.domain.authentication.UserRefreshTokenException
import com.cvix.identity.domain.authentication.error.AccountDisabledException
import com.cvix.identity.domain.authentication.error.FederatedAuthenticationException
import com.cvix.identity.domain.authentication.error.InvalidCredentialsException
import com.cvix.identity.domain.authentication.error.InvalidTokenException
import com.cvix.identity.domain.authentication.error.LogoutFailedException
import com.cvix.identity.domain.authentication.error.MissingCookieException
import com.cvix.identity.domain.authentication.error.NotAuthenticatedUserException
import com.cvix.identity.domain.authentication.error.RateLimitExceededException
import com.cvix.identity.domain.authentication.error.SessionNotFoundException
import com.cvix.identity.domain.authentication.error.UnknownAuthenticationException
import com.cvix.identity.infrastructure.authentication.cookie.AuthCookieBuilder
import java.net.URI
import java.time.Instant
import java.util.*
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.server.ServerWebExchange
import org.springframework.security.core.AuthenticationException as SpringAuthenticationException

/**
 * Handles rate limiting errors.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - DEFAULT_PRECEDENCE)
internal class RateLimitAdvice {

    /**
     * Handles exceptions of type RateLimitExceededException.
     *
     * @return ProblemDetail containing HTTP 429 status, error message, and retry-after information.
     */
    @ExceptionHandler(RateLimitExceededException::class)
    fun RateLimitExceededException.handleRateLimitExceeded(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS)
        detail.title = "rate limit exceeded"
        detail.setProperty(MESSAGE_KEY, "error.http.429")
        detail.setProperty("retryAfter", this.retryAfter.seconds)
        return detail
    }
}

/**
 * Handles token-related authentication errors.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - DEFAULT_PRECEDENCE)
internal class TokenAdvice {

    /**
     * Handles InvalidTokenException.
     *
     * @return ProblemDetail containing HTTP 401 status, error message, and timestamp.
     */
    @ExceptionHandler(InvalidTokenException::class)
    fun InvalidTokenException.handleInvalidToken(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "invalid token"
        detail.detail = this.message ?: "The provided token is invalid, expired, or malformed"
        detail.setProperty(MESSAGE_KEY, "error.auth.invalid_token")
        detail.setProperty(TIMESTAMP, Instant.now().toString())
        return detail
    }

    /**
     * Handles LogoutFailedException.
     *
     * @return ProblemDetail containing HTTP 400 status, error message, and timestamp.
     */
    @ExceptionHandler(LogoutFailedException::class)
    fun LogoutFailedException.handleLogoutFailed(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = "logout failed"
        detail.detail = this.message ?: "Failed to complete logout operation"
        detail.setProperty(MESSAGE_KEY, "error.auth.logout_failed")
        detail.setProperty(TIMESTAMP, Instant.now().toString())
        return detail
    }
}

/**
 * Handles credential validation errors.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - DEFAULT_PRECEDENCE)
internal class CredentialsAdvice {

    /**
     * Handles BadCredentialsException.
     *
     * @return ProblemDetail containing HTTP 401 status, error message, and timestamp.
     */
    @ExceptionHandler(BadCredentialsException::class)
    fun BadCredentialsException.handleBadCredentials(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "invalid credentials"
        detail.detail = "The provided email or password is incorrect"
        detail.setProperty(MESSAGE_KEY, "error.auth.invalid_credentials")
        detail.setProperty(TIMESTAMP, Instant.now().toString())
        return detail
    }

    /**
     * Handles InvalidCredentialsException.
     *
     * @return ProblemDetail containing HTTP 401 status, error message, and timestamp.
     */
    @ExceptionHandler(InvalidCredentialsException::class)
    fun InvalidCredentialsException.handleInvalidCredentials(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "invalid credentials"
        detail.detail = this.message ?: "The provided email or password is incorrect"
        detail.setProperty(MESSAGE_KEY, "error.auth.invalid_credentials")
        detail.setProperty(TIMESTAMP, Instant.now().toString())
        return detail
    }
}

/**
 * Handles session management errors.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - DEFAULT_PRECEDENCE)
internal class SessionAdvice {

    /**
     * Handles SessionNotFoundException.
     *
     * @return ProblemDetail containing HTTP 401 status, error message, and timestamp.
     */
    @ExceptionHandler(SessionNotFoundException::class)
    fun SessionNotFoundException.handleSessionNotFound(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "session not found"
        detail.detail = this.message ?: "Your session has expired. Please log in again."
        detail.setProperty(MESSAGE_KEY, "error.auth.session_not_found")
        detail.setProperty(TIMESTAMP, Instant.now().toString())
        return detail
    }
}

/**
 * Handles federated authentication errors.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - DEFAULT_PRECEDENCE)
internal class FederatedAuthAdvice {

    /**
     * Handles OAuth2AuthenticationException.
     *
     * @return ProblemDetail containing HTTP 401 status, error message, error code, and timestamp.
     */
    @ExceptionHandler(OAuth2AuthenticationException::class)
    fun OAuth2AuthenticationException.handleOAuth2AuthenticationException(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "federated authentication failed"
        detail.detail = this.error.description ?: "Failed to authenticate with the identity provider"
        detail.setProperty(MESSAGE_KEY, "error.auth.federated_auth_failed")
        detail.setProperty("error_code", this.error.errorCode)
        detail.setProperty(TIMESTAMP, Instant.now().toString())
        return detail
    }

    /**
     * Handles FederatedAuthenticationException.
     *
     * @return ProblemDetail containing HTTP 401 status, error message, provider and timestamp.
     */
    @ExceptionHandler(FederatedAuthenticationException::class)
    fun FederatedAuthenticationException.handleFederatedAuthenticationFailed(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "federated authentication failed"
        detail.detail = this.message ?: "Failed to authenticate with ${this.provider}"
        detail.setProperty(MESSAGE_KEY, "error.auth.federated_failed")
        detail.setProperty("provider", this.provider)
        detail.setProperty(TIMESTAMP, Instant.now().toString())
        return detail
    }
}

/**
 * Handles account status errors.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - DEFAULT_PRECEDENCE)
internal class AccountStatusAdvice {

    /**
     * Handles AccountDisabledException.
     *
     * @return ProblemDetail containing HTTP 403 status, error message, and timestamp.
     */
    @ExceptionHandler(AccountDisabledException::class)
    fun AccountDisabledException.handleAccountDisabled(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN)
        detail.title = "account disabled"
        detail.detail = this.message ?: "This account has been disabled or suspended"
        detail.setProperty(MESSAGE_KEY, "error.auth.account_disabled")
        detail.setProperty(TIMESTAMP, Instant.now().toString())
        return detail
    }
}

private const val AUTHENTICATION = "AUTHENTICATION"

/**
 * Handles general authentication errors.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - DEFAULT_PRECEDENCE)
internal class GeneralAuthAdvice {

    /**
     * Handles NotAuthenticatedUserException.
     *
     * @return ProblemDetail containing HTTP 401 status and error message.
     */
    @ExceptionHandler(NotAuthenticatedUserException::class)
    fun NotAuthenticatedUserException.handleNotAuthenticateUser(): ProblemDetail {
        val detail = createProblemDetail(
            status = HttpStatus.UNAUTHORIZED,
            title = "not authenticated",
            detail = null,
            typeSuffix = "authentication/not-authenticated",
            errorCategory = AUTHENTICATION,
            exchange = null,
            messageKey = "error.http.401",
            includeInstance = false,
        )
        return detail
    }

    /**
     * Handles UnknownAuthenticationException.
     *
     * @return ProblemDetail containing HTTP 500 status and error message.
     */
    @ExceptionHandler(UnknownAuthenticationException::class)
    fun UnknownAuthenticationException.handleUnknownAuthentication(): ProblemDetail {
        val detail = createProblemDetail(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            title = "unknown authentication",
            detail = null,
            typeSuffix = "authentication/unknown-authentication",
            errorCategory = AUTHENTICATION,
            exchange = null,
            messageKey = "error.http.500",
            includeInstance = false,
        )
        return detail
    }

    /**
     * Handles SpringAuthenticationException.
     *
     * @return ProblemDetail containing HTTP 401 status, error message, and timestamp.
     */
    @ExceptionHandler(SpringAuthenticationException::class)
    fun SpringAuthenticationException.handleAuthenticationException(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "authentication failed"
        detail.detail = this.message ?: "Authentication failed. Please check your credentials and try again."
        detail.setProperty(MESSAGE_KEY, "error.auth.authentication_failed")
        detail.setProperty(TIMESTAMP, Instant.now().toString())
        return detail
    }
}

/**
 * Handles user authentication exceptions.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - DEFAULT_PRECEDENCE)
internal class UserAuthAdvice(
    private val messageSource: MessageSource
) {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UserAuthenticationException::class, UserRefreshTokenException::class)
    fun handleUserAuthenticationException(
        e: Exception,
        response: ServerHttpResponse,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val localizedMessage = getLocalizedMessage(exchange, messageSource, MSG_AUTHENTICATION_FAILED)
        val problemDetail = createProblemDetail(
            status = HttpStatus.UNAUTHORIZED,
            title = "User authentication failed",
            detail = e.message,
            typeSuffix = "user-authentication-failed",
            errorCategory = AUTHENTICATION,
            exchange = exchange,
            messageKey = MSG_AUTHENTICATION_FAILED,
            localizedMessage = localizedMessage,
        )
        AuthCookieBuilder.clearCookies(response)
        return problemDetail
    }
}

/**
 * Handles missing cookie exceptions.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - DEFAULT_PRECEDENCE)
internal class CookieAdvice(
    private val messageSource: MessageSource
) {

    @ExceptionHandler(MissingCookieException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingCookieException(
        e: MissingCookieException,
        response: ServerHttpResponse,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val localizedMessage = getLocalizedMessage(exchange, messageSource, MSG_MISSING_COOKIE)
        response.statusCode = HttpStatus.BAD_REQUEST
        return createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = "Missing cookie",
            detail = e.message,
            typeSuffix = "missing-cookie",
            errorCategory = "MISSING_COOKIE",
            exchange = exchange,
            messageKey = MSG_MISSING_COOKIE,
            localizedMessage = localizedMessage,
        )
    }
}

private fun getLocalizedMessage(exchange: ServerWebExchange, messageSource: MessageSource, messageKey: String): String {
    val locale = exchange.localeContext.locale ?: Locale.getDefault()
    return try {
        messageSource.getMessage(messageKey, null, locale) ?: messageKey
    } catch (_: NoSuchMessageException) {
        messageKey
    }
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
