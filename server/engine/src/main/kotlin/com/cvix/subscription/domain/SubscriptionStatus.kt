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
     * The subscription has been cancelled and is immediately considered inactive.
     *
     * Note: Although a cancelled subscription may have a future valid_until date,
     * it cannot be used for access control or feature access. The cancellation is permanent.
     * To distinguish between "paid-for period" and "active for use", check the validUntil
     * field and use the Subscription.isValid() method respectively.
     */
    CANCELLED,

    /**
     * The subscription has expired and is no longer valid.
     */
    EXPIRED;

    /**
     * Checks if this subscription status represents an active state.
     *
     * Returns true only if the status is ACTIVE. This method does not consider the
     * validity period (start/end dates). For time-based validity checks, use
     * Subscription.isValid() instead.
     *
     * A cancelled subscription always returns false, even if its validUntil date
     * is in the future.
     */
    fun isActive(): Boolean = this == ACTIVE
}
