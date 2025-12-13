package com.cvix.subscription.domain

/**
 * Base exception for subscription-related errors.
 *
 * @created 12/11/25
 */
open class SubscriptionException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
