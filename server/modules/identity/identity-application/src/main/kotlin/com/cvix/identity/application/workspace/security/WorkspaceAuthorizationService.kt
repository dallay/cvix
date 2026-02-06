package com.cvix.identity.application.workspace.security

import com.cvix.common.domain.Service
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.identity.domain.workspace.WorkspaceAuthorizationException
import com.cvix.identity.domain.workspace.WorkspaceMemberRepository
import java.util.UUID

/**
 * Service responsible for managing workspace authorization.
 * Provides methods to ensure that a user has access to a specific workspace.
 *
 * @property workspaceMemberRepository Repository for checking workspace membership.
 */
@Service
class WorkspaceAuthorizationService(
    private val workspaceMemberRepository: WorkspaceMemberRepository
) : WorkspaceAuthorization {
    /**
     * Ensures that the user has access to the specified workspace.
     * Throws a [WorkspaceAuthorizationException] if the user does not have access.
     *
     * @param workspaceId The [java.util.UUID] of the workspace.
     * @param userId The [java.util.UUID] of the user.
     * @throws [WorkspaceAuthorizationException] If the user does not have access to the workspace.
     */
    override suspend fun ensureAccess(workspaceId: UUID, userId: UUID) {
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
    override suspend fun ensureAccess(workspaceId: String, userId: String) {
        val wsUuid = try {
            UUID.fromString(workspaceId)
        } catch (_: IllegalArgumentException) {
            throw WorkspaceAuthorizationException("Invalid workspaceId format: $workspaceId")
        }
        val userUuid = try {
            UUID.fromString(userId)
        } catch (_: IllegalArgumentException) {
            throw WorkspaceAuthorizationException("Invalid userId format: $userId")
        }
        ensureAccess(wsUuid, userUuid)
    }
}
