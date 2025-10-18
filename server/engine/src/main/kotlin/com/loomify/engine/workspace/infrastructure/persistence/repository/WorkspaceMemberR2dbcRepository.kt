package com.loomify.engine.workspace.infrastructure.persistence.repository

import com.loomify.engine.workspace.infrastructure.persistence.entity.WorkspaceMemberEntity
import com.loomify.engine.workspace.infrastructure.persistence.entity.WorkspaceMemberId
import java.util.*
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

/**
 * R2DBC repository for workspace members.
 * Note: Using custom queries due to composite key limitations in Spring Data R2DBC.
 */
@Repository
interface WorkspaceMemberR2dbcRepository : CoroutineCrudRepository<WorkspaceMemberEntity, WorkspaceMemberId> {
    /**
     * Finds all workspace members for a workspace.
     *
     * @param workspaceId The ID of the workspace.
     * @return A flux of workspace member entities.
     */
    @Query("SELECT workspace_id, user_id, role, created_at FROM workspace_members WHERE workspace_id = :workspaceId")
    suspend fun findByWorkspaceId(workspaceId: UUID): List<WorkspaceMemberEntity>

    /**
     * Finds all workspaces for a user.
     *
     * @param userId The ID of the user.
     * @return A flux of workspace member entities.
     */
    @Query("SELECT workspace_id, user_id, role, created_at FROM workspace_members WHERE user_id = :userId")
    suspend fun findByUserId(userId: UUID): List<WorkspaceMemberEntity>

    /**
     * Checks if a user is a member of a workspace.
     *
     * @param workspaceId The ID of the workspace.
     * @param userId The ID of the user.
     * @return True if the user is a member of the workspace, false otherwise.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM workspace_members WHERE workspace_id = :workspaceId AND user_id = :userId)")
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
    @Modifying
    @Query(
        """
        INSERT INTO workspace_members (workspace_id, user_id, role, created_at, updated_at)
        VALUES (:workspaceId, :userId, :role::role_type, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    """,
    )
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
    @Modifying
    @Query("DELETE FROM workspace_members WHERE workspace_id = :workspaceId AND user_id = :userId")
    suspend fun deleteByWorkspaceIdAndUserId(workspaceId: UUID, userId: UUID): Int
}
