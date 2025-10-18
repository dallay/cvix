package com.loomify.engine.authentication.domain.error

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
