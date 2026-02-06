package com.cvix.resume.infrastructure.persistence.repository

import com.cvix.resume.infrastructure.persistence.entity.ResumeEntity
import java.util.UUID
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data R2DBC repository for Resume persistence.
 * Provides reactive CRUD operations with coroutine support.
 */
@Repository
interface ResumeReactiveR2dbcRepository : CoroutineCrudRepository<ResumeEntity, UUID> {
    /**
     * Finds a resume by ID and user ID (for authorization).
     */
    suspend fun findByIdAndUserId(id: UUID, userId: UUID): ResumeEntity?

    /**
     * Finds all resumes for a user in a workspace, ordered by most recently updated.
     */
    @Query(
        """
        SELECT id, user_id, workspace_id, title, data, created_by, created_at, updated_by, updated_at
        FROM resumes
        WHERE user_id = :userId AND workspace_id = :workspaceId
        ORDER BY updated_at DESC, id DESC
        """,
    )
    suspend fun findByUserIdAndWorkspaceId(userId: UUID, workspaceId: UUID): List<ResumeEntity>

    /**
     * Deletes a resume by ID and user ID (for authorization).
     */
    suspend fun deleteByIdAndUserId(id: UUID, userId: UUID): Long

    /**
     * Checks if a resume exists by ID and user ID (for authorization).
     */
    suspend fun existsByIdAndUserId(id: UUID, userId: UUID): Boolean
}
