package com.cvix.contact.domain

/**
 * Base exception for contact domain operations.
 */
sealed class ContactException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Thrown when CAPTCHA validation fails.
 */
class CaptchaValidationException(
    message: String = "CAPTCHA validation failed",
    cause: Throwable? = null
) : ContactException(message, cause)

/**
 * Thrown when contact notification fails.
 */
class ContactNotificationException(
    message: String = "Failed to send contact notification",
    cause: Throwable? = null
) : ContactException(message, cause)
