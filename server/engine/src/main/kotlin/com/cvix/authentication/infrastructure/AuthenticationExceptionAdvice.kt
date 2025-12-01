package com.cvix.authentication.infrastructure

import com.cvix.authentication.domain.error.AccountDisabledException
import com.cvix.authentication.domain.error.FederatedAuthenticationException
import com.cvix.authentication.domain.error.InvalidCredentialsException
import com.cvix.authentication.domain.error.InvalidTokenException
import com.cvix.authentication.domain.error.LogoutFailedException
import com.cvix.authentication.domain.error.NotAuthenticatedUserException
import com.cvix.authentication.domain.error.RateLimitExceededException
import com.cvix.authentication.domain.error.SessionNotFoundException
import com.cvix.authentication.domain.error.UnknownAuthenticationException
import com.cvix.controllers.DEFAULT_PRECEDENCE
import com.cvix.controllers.MESSAGE_KEY
import com.cvix.controllers.TIMESTAMP
import java.time.Instant
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
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
 * Handles validation and input errors.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - DEFAULT_PRECEDENCE)
internal class ValidationAdvice {

    /**
     * Handles WebExchangeBindException, typically caused by validation errors.
     *
     * @return ProblemDetail containing HTTP 400 status, error message, and field-specific validation errors.
     */
    @ExceptionHandler(WebExchangeBindException::class)
    fun WebExchangeBindException.handleValidationException(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = "validation failed"
        detail.detail = "Request validation failed. Please check the provided data."
        detail.setProperty(MESSAGE_KEY, "error.validation.failed")
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
    fun IllegalArgumentException.handleIllegalArgument(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = "invalid input"
        detail.detail = this.message ?: "The provided input is invalid"
        detail.setProperty(MESSAGE_KEY, "error.invalid_input")
        detail.setProperty(TIMESTAMP, Instant.now().toString())
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
        val detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
        detail.title = "not authenticated"
        detail.setProperty(MESSAGE_KEY, "error.http.401")
        return detail
    }

    /**
     * Handles UnknownAuthenticationException.
     *
     * @return ProblemDetail containing HTTP 500 status and error message.
     */
    @ExceptionHandler(UnknownAuthenticationException::class)
    fun UnknownAuthenticationException.handleUnknownAuthentication(): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        detail.title = "unknown authentication"
        detail.setProperty(MESSAGE_KEY, "error.http.500")
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
