package com.cvix.subscriber.domain

/**
 * Value class representing the source of a subscription.
 *
 * This class wraps a string value indicating where the subscription originated from,
 * such as a specific platform, campaign, or referral.
 *
 * @property source The source identifier for the subscription.
 */
@JvmInline
value class SubscriptionSource(val source: String) {
    init {
        require(source.isNotBlank()) { "Subscription source cannot be blank" }
    }
    /**
     * Returns the string representation of the subscription source.
     *
     * @return The source as a string.
     */
    override fun toString(): String = source
}
