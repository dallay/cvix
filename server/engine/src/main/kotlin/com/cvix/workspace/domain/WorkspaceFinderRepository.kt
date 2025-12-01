package com.cvix.workspace.domain

import com.cvix.users.domain.UserId

/**
 * Repository interface for finding workspaces.
 */
interface WorkspaceFinderRepository {

    /**
     * Finds a workspace by its ID.
     *
     * @param id The ID of the workspace to find.
     * @return The workspace, or null if not found.
     */
    suspend fun findById(id: WorkspaceId): Workspace?

    /**
     * Finds all workspaces.
     *
     * @return A list of all workspaces.
     */
    suspend fun findAll(): List<Workspace>

    /**
     * Finds all workspaces for a user.
     *
     * @param userId The ID of the user.
     * @return A list of workspaces that the user is a member of.
     */
    suspend fun findByMemberId(userId: UserId): List<Workspace>

    /**
     * Finds all workspaces owned by a user.
     *
     * @param userId The ID of the user.
     * @return A list of workspaces that the user owns.
     */
    suspend fun findByOwnerId(userId: UserId): List<Workspace>
}
