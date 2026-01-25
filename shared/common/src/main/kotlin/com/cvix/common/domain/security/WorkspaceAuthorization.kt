package com.cvix.common.domain.security

import java.util.UUID

/**
 * Defines authorization checks for workspace access.
 *
 * Implementations should ensure that a user has permission to access a given workspace,
 * throwing an exception if access is denied.
 */
interface WorkspaceAuthorization {
    /**
     * Ensures the user with the given UUID has access to the specified workspace.
     *
     * @param workspaceId The unique identifier of the workspace.
     * @param userId The unique identifier of the user.
     * @throws SecurityException if the user does not have access.
     */
    suspend fun ensureAccess(workspaceId: UUID, userId: UUID)

    /**
     * Ensures the user with the given String ID has access to the specified workspace.
     *
     * @param workspaceId The string identifier of the workspace.
     * @param userId The string identifier of the user.
     * @throws SecurityException if the user does not have access.
     */
    suspend fun ensureAccess(workspaceId: String, userId: String)
}
