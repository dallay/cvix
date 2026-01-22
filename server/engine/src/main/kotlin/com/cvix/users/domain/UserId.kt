package com.cvix.users.domain

import java.util.UUID

/**
 * Value class representing a unique identifier for a [User].
 *
 * @property value The underlying UUID value.
 */
@JvmInline
value class UserId(val value: UUID) {

    override fun toString(): String = value.toString()

    companion object {
        /**
         * Generates a new random UserId.
         *
         * @return A UserId with a randomly generated UUID.
         */
        fun random(): UserId = UserId(UUID.randomUUID())
        /**
         * Creates a UserId from a string representation of a UUID.
         *
         * @param id The string representation of the UUID.
         * @return A UserId corresponding to the given UUID string.
         */
        fun fromString(id: String): UserId = UserId(UUID.fromString(id))
    }
}
