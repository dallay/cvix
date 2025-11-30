package com.cvix.authentication.domain.error

/**
 * Represents an exception that occurs during the authentication process.
 *
 * This class is a sealed class, meaning it can only be extended within the same file.
 * It extends the RuntimeException class which is an unchecked exception.
 *
 * @param message A detailed message describing the authentication error.
 * @param cause The underlying exception that caused this authentication error.
 */
sealed class AuthenticationException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * An exception that is thrown when a user is not authenticated.
 * @param message A detailed message describing the authentication error.
 * @param cause The underlying exception that caused this authentication error.
 * @see AuthenticationException for more information.
 */
class NotAuthenticatedUserException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

/**
 * An exception that is thrown when an unknown authentication scheme is used.
 * @param message A detailed message describing the authentication error.
 * @param cause The underlying exception that caused this authentication error.
 * @see AuthenticationException for more information.
 */
class UnknownAuthenticationException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

/**
 * An exception that is thrown when logout fails.
 * @param message A message describing the exception.
 * @param cause The exception that caused the logout to fail.
 */
class LogoutFailedException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

/**
 * An exception that is thrown when a token is invalid, expired, or malformed.
 * This exception is typically thrown during token validation or processing.
 * @param message A detailed message describing the token validation issue.
 * @param cause The underlying exception that caused the token validation failure (optional).
 */
class InvalidTokenException(
    message: String? = null,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

/**
 * An exception that is thrown when invalid credentials are provided.
 * @param message A detailed message describing the invalid credentials issue.
 * @param cause The underlying exception that caused this authentication error (optional).
 */
class InvalidCredentialsException(
    message: String? = "Invalid email or password",
    cause: Throwable? = null
) : AuthenticationException(message, cause)

/**
 * An exception that is thrown when a user account is disabled or suspended.
 * @param message A detailed message describing why the account is disabled.
 * @param cause The underlying exception that caused this authentication error (optional).
 */
class AccountDisabledException(
    message: String? = "This account has been disabled",
    cause: Throwable? = null
) : AuthenticationException(message, cause)

/**
 * An exception that is thrown when federated authentication fails.
 * @param provider The identity provider name (e.g., "google", "microsoft", "github").
 * @param message A detailed message describing the federated authentication failure.
 * @param cause The underlying exception that caused this authentication error (optional).
 */
class FederatedAuthenticationException(
    val provider: String,
    message: String? = "Failed to authenticate with $provider",
    cause: Throwable? = null
) : AuthenticationException(message, cause)

/**
 * An exception that is thrown when a session is not found or has expired.
 * @param message A detailed message describing the session issue.
 * @param cause The underlying exception that caused this authentication error (optional).
 */
class SessionNotFoundException(
    message: String? = "Session not found or expired",
    cause: Throwable? = null
) : AuthenticationException(message, cause)
