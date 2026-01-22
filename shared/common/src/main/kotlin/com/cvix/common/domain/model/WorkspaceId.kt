package com.cvix.common.domain.model

import java.util.UUID

/**
 * Value class representing a unique identifier for a Workspace.
 *
 * @property value The underlying UUID value.
 */
@JvmInline
value class WorkspaceId(val value: UUID) {
    companion object {
        /**
         * Generates a new random WorkspaceId.
         *
         * @return A WorkspaceId with a randomly generated UUID.
         */
        fun random(): WorkspaceId = WorkspaceId(UUID.randomUUID())
    }
}
