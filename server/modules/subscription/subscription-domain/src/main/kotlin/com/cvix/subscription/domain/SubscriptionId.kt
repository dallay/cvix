package com.cvix.subscription.domain

import java.util.UUID

/**
 * Value class representing a unique identifier for a [Subscription].
 *
 * @property value The underlying UUID value.
 */
@JvmInline
value class SubscriptionId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        /**
         * Generates a new random SubscriptionId.
         *
         * @return A SubscriptionId with a randomly generated UUID.
         */
        fun random(): SubscriptionId = SubscriptionId(UUID.randomUUID())
        /**
         * Creates a SubscriptionId from a string representation of a UUID.
         *
         * @param id The string representation of the UUID.
         * @return A SubscriptionId corresponding to the given UUID string.
         */
        fun fromString(id: String): SubscriptionId = SubscriptionId(UUID.fromString(id))
    }
}
