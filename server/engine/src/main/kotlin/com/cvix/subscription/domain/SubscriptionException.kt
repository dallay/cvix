package com.cvix.subscription.domain

/**
 * Base exception for subscription-related errors.
 *
 * This exception is intended to be extended by specific subscription domain exceptions
 * such as TemplateNotFoundException or TemplateAccessDeniedException.
 *
 * @since 1.0.0
 */
open class SubscriptionException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    constructor(cause: Throwable) : this(cause.message ?: "Subscription error", cause)
}
