package com.cvix.subscription.domain

import com.cvix.common.domain.AggregateRoot
import java.time.Instant
import java.util.UUID

/**
 * Subscription aggregate root representing a user's subscription plan.
 *
 * A subscription defines:
 * - The subscription tier (FREE, BASIC, PROFESSIONAL)
 * - The user who owns the subscription
 * - The validity period (start and optional end date)
 * - The current status (ACTIVE, CANCELLED, EXPIRED)
 *
 * Semantic Clarity:
 * - **Status** (ACTIVE/CANCELLED/EXPIRED): Represents the lifecycle state. Cancelled means the
 *   subscription is no longer usable, even if the validity period extends into the future.
 * - **Validity Period** (validFrom/validUntil): Represents the paid-for time window. A cancelled
 *   subscription still has a valid period if its validUntil date is in the future, but it cannot
 *   be used for access control.
 * - **isValid()**: Returns true only if BOTH the status is ACTIVE AND the current time is within
 *   the validity period. Use this to check if a subscription grants access.
 * - **isActive()**: Returns true only if the status is ACTIVE. Does not consider time.
 *
 * Business rules:
 * - A user can only have one active subscription at a time
 * - A subscription without an end date is considered perpetual (until cancelled)
 * - Expired subscriptions cannot be reactivated (a new subscription must be created)
 * - Cancelled subscriptions are immediately inactive, regardless of their validity period
 *
 * @param id The unique identifier for this subscription
 * @param userId The UUID of the user who owns this subscription
 * @param tier The subscription tier
 * @param status The current status of the subscription
 * @param validFrom The start date of the subscription
 * @param validUntil The optional end date of the subscription (null means perpetual)
 * @created 12/11/25
 */
data class Subscription(
    override val id: SubscriptionId,
    val userId: UUID,
    val tier: SubscriptionTier,
    val status: SubscriptionStatus,
    val validFrom: Instant,
    val validUntil: Instant? = null
) : AggregateRoot<SubscriptionId>() {

    /**
     * Checks if this subscription is currently valid based on the current time.
     *
     * A subscription is valid if:
     * - Status is ACTIVE (cancelled and expired subscriptions are never valid)
     * - Current time is after validFrom
     * - Current time is before validUntil (or validUntil is null for perpetual subscriptions)
     *
     * This method combines both the subscription status and the validity period to determine
     * if the subscription can be used for access control or feature access. A cancelled
     * subscription is never valid, regardless of its validity period.
     */
    fun isValid(now: Instant = Instant.now()): Boolean {
        if (!status.isActive()) return false
        if (now.isBefore(validFrom)) return false
        if (validUntil != null && now.isAfter(validUntil)) return false
        return true
    }

    /**
     * Checks if this subscription is expired based on the current time.
     */
    fun isExpired(now: Instant = Instant.now()): Boolean {
        val isExpiredByDate = validUntil != null && now.isAfter(validUntil)
        return status == SubscriptionStatus.EXPIRED || isExpiredByDate
    }

    /**
     * Cancels this subscription.
     *
     * @return A new Subscription instance with CANCELLED status
     */
    fun cancel(): Subscription = copy(status = SubscriptionStatus.CANCELLED)

    /**
     * Marks this subscription as expired.
     *
     * @return A new Subscription instance with EXPIRED status
     */
    fun expire(): Subscription = copy(status = SubscriptionStatus.EXPIRED)

    companion object {
        /**
         * Creates a new active subscription.
         *
         * @param userId The UUID of the user who will own this subscription
         * @param tier The subscription tier
         * @param validFrom The start date (defaults to now)
         * @param validUntil Optional end date (null means perpetual)
         * @return A new Subscription instance
         */
        fun create(
            userId: UUID,
            tier: SubscriptionTier,
            validFrom: Instant = Instant.now(),
            validUntil: Instant? = null
        ): Subscription = Subscription(
            id = SubscriptionId.random(),
            userId = userId,
            tier = tier,
            status = SubscriptionStatus.ACTIVE,
            validFrom = validFrom,
            validUntil = validUntil,
        )

        /**
         * Creates a default FREE tier subscription for a user.
         *
         * @param userId The UUID of the user who will own this subscription
         * @return A new perpetual FREE tier Subscription
         */
        fun createFree(userId: UUID): Subscription = create(
            userId = userId,
            tier = SubscriptionTier.FREE,
            validFrom = Instant.now(),
            validUntil = null,
        )
    }
}
