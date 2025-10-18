package com.loomify.engine.workspace.domain

/**
 * Repository interface for workspace persistence.
 */
interface WorkspaceRepository {
    /**
     * Creates a new workspace.
     *
     * @param workspace The workspace to create.
     */
    suspend fun create(workspace: Workspace)

    /**
     * Updates a workspace.
     *
     * @param workspace The workspace to update.
     */
    suspend fun update(workspace: Workspace)

    /**
     * Deletes a workspace.
     *
     * @param id The ID of the workspace to delete.
     */
    suspend fun delete(id: WorkspaceId)
}
