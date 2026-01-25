package com.cvix.form.domain

import java.util.UUID

/**
 * Value class representing a unique identifier for a Subscription Form.
 *
 * This class wraps a [UUID] to provide type safety and domain specificity for subscription form IDs.
 *
 * @property value The underlying UUID value.
 * @constructor Creates a [SubscriptionFormId] with the specified [UUID].
 * @created 25/1/26
 */
@JvmInline
value class SubscriptionFormId(val value: UUID) {
    /**
     * Returns the string representation of the underlying UUID.
     *
     * @return UUID as a [String].
     */
    override fun toString(): String = value.toString()

    companion object {
        /**
         * Creates a [SubscriptionFormId] from a string representation of a UUID.
         *
         * @param value The string representation of the UUID.
         * @return A new [SubscriptionFormId] instance.
         * @throws IllegalArgumentException if the string is not a valid UUID.
         */
        fun fromString(value: String): SubscriptionFormId =
            SubscriptionFormId(UUID.fromString(value))

        /**
         * Generates a new [SubscriptionFormId] with a random UUID.
         *
         * @return A new [SubscriptionFormId] instance with a random UUID.
         */
        fun random(): SubscriptionFormId =
            SubscriptionFormId(UUID.randomUUID())
    }
}
