package com.cvix.identity.infrastructure.workspace.persistence.entity

import java.io.Serializable
import java.util.UUID

/**
 * Represents the composite primary key for WorkspaceMemberEntity.
 */
data class WorkspaceMemberId(
    val workspaceId: UUID,
    val userId: UUID
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
