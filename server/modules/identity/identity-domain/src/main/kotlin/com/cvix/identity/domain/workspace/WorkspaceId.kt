package com.cvix.identity.domain.workspace

import java.util.UUID

/**
 * Value class representing a unique identifier for a [Workspace].
 *
 * @property value The underlying UUID value.
 */
@JvmInline
value class WorkspaceId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        /**
         * Generates a new random WorkspaceId.
         *
         * @return A WorkspaceId with a randomly generated UUID.
         */
        fun random(): WorkspaceId = WorkspaceId(UUID.randomUUID())
        /**
         * Creates a WorkspaceId from a string representation of a UUID.
         *
         * @param id The string representation of the UUID.
         * @return A WorkspaceId corresponding to the given UUID string.
         */
        fun fromString(id: String): WorkspaceId = WorkspaceId(UUID.fromString(id))
    }
}
