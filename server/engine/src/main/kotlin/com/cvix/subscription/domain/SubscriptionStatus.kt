package com.cvix.subscription.domain

/**
 * Represents the status of a subscription.
 *
 * @created 12/11/25
 */
enum class SubscriptionStatus {
    /**
     * The subscription is currently active and valid.
     */
    ACTIVE,

    /**
     * The subscription has been cancelled but may still be valid until the end date.
     */
    CANCELLED,

    /**
     * The subscription has expired and is no longer valid.
     */
    EXPIRED;

    /**
     * Checks if this subscription status represents an active state.
     */
    fun isActive(): Boolean = this == ACTIVE
}
