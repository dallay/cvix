package com.cvix.workspace.domain

import java.io.Serializable
import java.util.UUID

/**
 * Composite identifier para workspace member relationship.
 * Leverages immutability patterns para consistency del domain model.
 */
data class WorkspaceMemberId(
    val workspaceId: UUID,
    val userId: UUID
) : Serializable {
    override fun toString() = "WorkspaceMemberId($workspaceId::$userId)"
    companion object {
        private const val serialVersionUID = 1L

        fun create(workspaceId: UUID, userId: UUID) =
            WorkspaceMemberId(workspaceId, userId)

        fun create(workspaceId: String, userId: String) =
            WorkspaceMemberId(
                UUID.fromString(workspaceId),
                UUID.fromString(userId),
            )
    }
}
