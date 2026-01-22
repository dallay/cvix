package com.cvix.subscriber.domain

import java.util.UUID

/**
 * Value class representing a unique identifier for a [Subscriber].
 *
 * @property value The underlying UUID value.
 */
@JvmInline
value class SubscriberId(val value: UUID) {
    companion object {
        /**
         * Generates a new random SubscriberId.
         *
         * @return A SubscriberId with a randomly generated UUID.
         */
        fun random(): SubscriberId = SubscriberId(UUID.randomUUID())
    }
}
