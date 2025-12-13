package com.cvix.workspace

import com.cvix.common.domain.Service
import com.cvix.workspace.domain.WorkspaceAuthorizationException
import com.cvix.workspace.domain.WorkspaceMemberRepository
import java.util.*

/**
 * Service responsible for managing workspace authorization.
 * Provides methods to ensure that a user has access to a specific workspace.
 *
 * @property workspaceMemberRepository Repository for checking workspace membership.
 */
@Service
class WorkspaceAuthorizationService(
    private val workspaceMemberRepository: WorkspaceMemberRepository
) {
    /**
     * Ensures that the user has access to the specified workspace.
     * Throws a [WorkspaceAuthorizationException] if the user does not have access.
     *
     * @param workspaceId The [UUID] of the workspace.
     * @param userId The [UUID] of the user.
     * @throws [WorkspaceAuthorizationException] If the user does not have access to the workspace.
     */
    suspend fun ensureAccess(workspaceId: UUID, userId: UUID) {
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw WorkspaceAuthorizationException("User $userId has no access to workspace $workspaceId")
        }
    }

    /**
     * Ensures that the user has access to the specified workspace.
     * This is an overloaded method that accepts workspace and user IDs as strings.
     * Converts the string IDs to UUIDs and delegates to the other `ensureAccess` method.
     *
     * @param workspaceId The string representation of the workspace [UUID].
     * @param userId The string representation of the user [UUID].
     * @throws [WorkspaceAuthorizationException] If the user does not have access to the workspace.
     */
    suspend fun ensureAccess(workspaceId: String, userId: String) {
        ensureAccess(UUID.fromString(workspaceId), UUID.fromString(userId))
    }
}
