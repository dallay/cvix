package com.loomify.engine.authentication.infrastructure

import com.loomify.engine.authentication.domain.error.AccountDisabledException
import com.loomify.engine.authentication.domain.error.FederatedAuthenticationException
import com.loomify.engine.authentication.domain.error.InvalidCredentialsException
import com.loomify.engine.authentication.domain.error.InvalidTokenException
import com.loomify.engine.authentication.domain.error.LogoutFailedException
import com.loomify.engine.authentication.domain.error.NotAuthenticatedUserException
import com.loomify.engine.authentication.domain.error.RateLimitExceededException
import com.loomify.engine.authentication.domain.error.SessionNotFoundException
import com.loomify.engine.authentication.domain.error.UnknownAuthenticationException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException as SpringAuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import java.time.Instant

private const val DEFAULT_PRECEDENCE = 2000

/**
 * Centralized exception handler for all authentication-related errors.
 *
 * Provides consistent error responses for authentication failures, token issues,
 * rate limiting, validation errors, and OAuth2/OIDC errors.
 *
 * **Requirements**: FR-013 (error handling), T039 (centralized error handling)
 *
 * Orders the execution of advice methods according to precedence.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - DEFAULT_PRECEDENCE)
internal class AuthenticationExceptionAdvice {
    /**
     * Handles the [RateLimitExceededException] and returns a ProblemDetail object.
     *
     * @return A [ProblemDetail] object with the status code set to TOO_MANY_REQUESTS, title set to "rate limit exceeded",
     * and the property [MESSAGE_KEY] set to "error.http.429".
     */
    @ExceptionHandler(RateLimitExceededException::class)
    fun RateLimitExceededException.handleRateLimitExceeded(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS)
        detail.title = "rate limit exceeded"
        detail.setProperty(MESSAGE_KEY, "error.http.429")
        detail.setProperty("retryAfter", this.retryAfter.seconds)
        return detail
    }

    /**
     * Handles the [NotAuthenticatedUserException] and returns a ProblemDetail object.
     *
     * @return A [ProblemDetail] object with the status code set to UNAUTHORIZED, title set to "not authenticated",
     * and the property [MESSAGE_KEY] set to "error.http.401".
     */
    @ExceptionHandler(NotAuthenticatedUserException::class)
    fun NotAuthenticatedUserException.handleNotAuthenticateUser(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "not authenticated"
        detail.setProperty(MESSAGE_KEY, "error.http.401")
        return detail
    }

    /**
     * Handles [UnknownAuthenticationException] and returns a [ProblemDetail] object with appropriate details.
     *
     * @return The [ProblemDetail] object representing the unknown authentication error.
     */
    @ExceptionHandler(UnknownAuthenticationException::class)
    fun UnknownAuthenticationException.handleUnknownAuthentication(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        detail.title = "unknown authentication"
        detail.setProperty(MESSAGE_KEY, "error.http.500")
        return detail
    }

    /**
     * Handles [InvalidTokenException] and returns a ProblemDetail object.
     *
     * @return A [ProblemDetail] object with status UNAUTHORIZED and appropriate error details.
     */
    @ExceptionHandler(InvalidTokenException::class)
    fun InvalidTokenException.handleInvalidToken(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "invalid token"
        detail.detail = this.message ?: "The provided token is invalid, expired, or malformed"
        detail.setProperty(MESSAGE_KEY, "error.auth.invalid_token")
        detail.setProperty("timestamp", Instant.now().toString())
        return detail
    }

    /**
     * Handles [LogoutFailedException] and returns a ProblemDetail object.
     *
     * @return A [ProblemDetail] object with status BAD_REQUEST.
     */
    @ExceptionHandler(LogoutFailedException::class)
    fun LogoutFailedException.handleLogoutFailed(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = "logout failed"
        detail.detail = this.message ?: "Failed to complete logout operation"
        detail.setProperty(MESSAGE_KEY, "error.auth.logout_failed")
        detail.setProperty("timestamp", Instant.now().toString())
        return detail
    }

    /**
     * Handles Spring Security [BadCredentialsException] for invalid login credentials.
     *
     * @return A [ProblemDetail] object with status UNAUTHORIZED.
     */
    @ExceptionHandler(BadCredentialsException::class)
    fun BadCredentialsException.handleBadCredentials(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "invalid credentials"
        detail.detail = "The provided email or password is incorrect"
        detail.setProperty(MESSAGE_KEY, "error.auth.invalid_credentials")
        detail.setProperty("timestamp", Instant.now().toString())
        return detail
    }

    /**
     * Handles Spring Security [SpringAuthenticationException] for general authentication failures.
     *
     * @return A [ProblemDetail] object with status UNAUTHORIZED.
     */
    @ExceptionHandler(SpringAuthenticationException::class)
    fun SpringAuthenticationException.handleAuthenticationException(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "authentication failed"
        detail.detail = this.message ?: "Authentication failed. Please check your credentials and try again."
        detail.setProperty(MESSAGE_KEY, "error.auth.authentication_failed")
        detail.setProperty("timestamp", Instant.now().toString())
        return detail
    }

    /**
     * Handles OAuth2 [OAuth2AuthenticationException] for federated login failures.
     *
     * @return A [ProblemDetail] object with status UNAUTHORIZED.
     */
    @ExceptionHandler(OAuth2AuthenticationException::class)
    fun OAuth2AuthenticationException.handleOAuth2AuthenticationException(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "federated authentication failed"
        detail.detail = this.error.description ?: "Failed to authenticate with the identity provider"
        detail.setProperty(MESSAGE_KEY, "error.auth.federated_auth_failed")
        detail.setProperty("error_code", this.error.errorCode)
        detail.setProperty("timestamp", Instant.now().toString())
        return detail
    }

    /**
     * Handles [WebExchangeBindException] for request validation errors.
     *
     * @return A [ProblemDetail] object with status BAD_REQUEST and field-specific validation errors.
     */
    @ExceptionHandler(WebExchangeBindException::class)
    fun WebExchangeBindException.handleValidationException(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = "validation failed"
        detail.detail = "Request validation failed. Please check the provided data."
        detail.setProperty(MESSAGE_KEY, "error.validation.failed")

        // Extract field errors
        val fieldErrors = this.bindingResult.fieldErrors.map { fieldError ->
            mapOf(
                "field" to fieldError.field,
                "message" to (fieldError.defaultMessage ?: "Invalid value"),
                "rejectedValue" to fieldError.rejectedValue
            )
        }

        detail.setProperty("errors", fieldErrors)
        detail.setProperty("timestamp", Instant.now().toString())
        return detail
    }

    /**
     * Handles generic [IllegalArgumentException] for invalid input.
     *
     * @return A [ProblemDetail] object with status BAD_REQUEST.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun IllegalArgumentException.handleIllegalArgument(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = "invalid input"
        detail.detail = this.message ?: "The provided input is invalid"
        detail.setProperty(MESSAGE_KEY, "error.invalid_input")
        detail.setProperty("timestamp", Instant.now().toString())
        return detail
    }

    /**
     * Handles [InvalidCredentialsException] for incorrect email/password.
     *
     * @return A [ProblemDetail] object with status UNAUTHORIZED.
     */
    @ExceptionHandler(InvalidCredentialsException::class)
    fun InvalidCredentialsException.handleInvalidCredentials(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "invalid credentials"
        detail.detail = this.message ?: "The provided email or password is incorrect"
        detail.setProperty(MESSAGE_KEY, "error.auth.invalid_credentials")
        detail.setProperty("timestamp", Instant.now().toString())
        return detail
    }

    /**
     * Handles [AccountDisabledException] for disabled or suspended accounts.
     *
     * @return A [ProblemDetail] object with status FORBIDDEN.
     */
    @ExceptionHandler(AccountDisabledException::class)
    fun AccountDisabledException.handleAccountDisabled(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN)
        detail.title = "account disabled"
        detail.detail = this.message ?: "This account has been disabled or suspended"
        detail.setProperty(MESSAGE_KEY, "error.auth.account_disabled")
        detail.setProperty("timestamp", Instant.now().toString())
        return detail
    }

    /**
     * Handles [FederatedAuthenticationException] for OAuth2/OIDC failures.
     *
     * @return A [ProblemDetail] object with status UNAUTHORIZED.
     */
    @ExceptionHandler(FederatedAuthenticationException::class)
    fun FederatedAuthenticationException.handleFederatedAuthenticationFailed(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "federated authentication failed"
        detail.detail = this.message ?: "Failed to authenticate with ${this.provider}"
        detail.setProperty(MESSAGE_KEY, "error.auth.federated_failed")
        detail.setProperty("provider", this.provider)
        detail.setProperty("timestamp", Instant.now().toString())
        return detail
    }

    /**
     * Handles [SessionNotFoundException] for expired or missing sessions.
     *
     * @return A [ProblemDetail] object with status UNAUTHORIZED.
     */
    @ExceptionHandler(SessionNotFoundException::class)
    fun SessionNotFoundException.handleSessionNotFound(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "session not found"
        detail.detail = this.message ?: "Your session has expired. Please log in again."
        detail.setProperty(MESSAGE_KEY, "error.auth.session_not_found")
        detail.setProperty("timestamp", Instant.now().toString())
        return detail
    }

    companion object {
        private const val MESSAGE_KEY = "message"
    }
}
