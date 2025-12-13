package com.cvix.workspace.domain

import com.cvix.common.domain.BaseId
import com.cvix.common.domain.Generated
import java.util.UUID

/**
 * The id of a workspace member.
 * @param workspaceId The workspace id.
 * @param userId The user id.
 */
data class WorkspaceMemberId(
    val workspaceId: UUID,
    val userId: UUID
) : BaseId<Pair<UUID, UUID>>(workspaceId to userId) {
    /**
     * Constructor to create a workspace member id.
     * @param workspaceId The workspace id.
     * @param userId The user id.
     */
    constructor(workspaceId: String, userId: String) : this(
        UUID.fromString(workspaceId),
        UUID.fromString(userId),
    )

    @Generated
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WorkspaceMemberId) return false
        return workspaceId == other.workspaceId && userId == other.userId
    }

    @Generated
    override fun hashCode(): Int = 31 * workspaceId.hashCode() + userId.hashCode()

    @Generated
    override fun toString(): String = "WorkspaceMemberId(workspaceId=$workspaceId, userId=$userId)"

    companion object {
        /**
         * Creates a new workspace member id with the specified workspaceId and userId.
         * @param workspaceId The workspace id.
         * @param userId The user id.
         * @return The newly created workspace member id.
         */
        fun create(workspaceId: UUID, userId: UUID) = WorkspaceMemberId(workspaceId, userId)

        /**
         * Creates a new workspace member id with the specified workspaceId and userId.
         * @param workspaceId The workspace id.
         * @param userId The user id.
         * @return The newly created workspace member id.
         */
        fun create(workspaceId: String, userId: String) =
            WorkspaceMemberId(UUID.fromString(workspaceId), UUID.fromString(userId))
    }
}
