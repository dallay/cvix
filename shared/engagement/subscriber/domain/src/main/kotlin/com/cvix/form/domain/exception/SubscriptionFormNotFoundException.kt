package com.cvix.form.domain.exception

import com.cvix.common.domain.error.EntityNotFoundException

/**
 * Exception thrown when a subscription form is not found.
 */
class SubscriptionFormNotFoundException(
    override val message: String,
    override val cause: Throwable? = null
) : EntityNotFoundException(message, cause)
