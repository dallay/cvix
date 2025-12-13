package com.cvix.workspace.domain

import java.util.UUID

/**
 *
 * @created 9/6/25
 */
interface WorkspaceMemberRepository {
    /**
     * Finds all workspace members for a workspace.
     *
     * @param workspaceId The ID of the workspace.
     * @return A flux of workspace member entities.
     */
    suspend fun findByWorkspaceId(workspaceId: UUID): List<WorkspaceMember>

    /**
     * Finds all workspaces for a user.
     *
     * @param userId The ID of the user.
     * @return A flux of workspace member entities.
     */
    suspend fun findByUserId(userId: UUID): List<WorkspaceMember>

    /**
     * Checks if a user is a member of a workspace.
     *
     * @param workspaceId The ID of the workspace.
     * @param userId The ID of the user.
     * @return True if the user is a member of the workspace, false otherwise.
     */
    suspend fun existsByWorkspaceIdAndUserId(workspaceId: UUID, userId: UUID): Boolean

    /**
     * Inserts a workspace member using a custom query.
     * This is needed due to composite key limitations in Spring Data R2DBC.
     *
     * @param workspaceId The ID of the workspace.
     * @param userId The ID of the user.
     * @param role The role of the user in the workspace.
     * @return The number of affected rows.
     */
    suspend fun insertWorkspaceMember(
        workspaceId: UUID,
        userId: UUID,
        role: String
    ): Int

    /**
     * Deletes a workspace member using a custom query.
     * This is needed due to composite key limitations in Spring Data R2DBC.
     *
     * @param workspaceId The ID of the workspace.
     * @param userId The ID of the user.
     * @return The number of affected rows.
     */
    suspend fun deleteByWorkspaceIdAndUserId(workspaceId: UUID, userId: UUID): Int
}
