package com.loomify.workspace.infrastructure.persistence.repository

import com.loomify.spring.boot.repository.ReactiveSearchRepository
import com.loomify.workspace.infrastructure.persistence.entity.WorkspaceEntity
import java.util.*
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkspaceR2dbcRepository :
    CoroutineCrudRepository<WorkspaceEntity, UUID>,
    ReactiveSearchRepository<WorkspaceEntity> {
    /**
     * Finds all workspaces owned by a user.
     *
     * @param ownerId The ID of the owner.
     * @return A flux of workspace entities.
     */
    fun findByOwnerId(ownerId: UUID): Flow<WorkspaceEntity>

    /**
     * Finds all workspaces where the given ID is part of the memberIds collection.
     *
     * @param memberId The ID of the member.
     * @return A flow of workspace entities.
     */
    @Query(
        """
      SELECT w.id,
             w.name,
             w.description,
             w.owner_id,
             w.created_by,
             w.created_at,
             w.updated_by,
             w.updated_at
      FROM workspaces w
      INNER JOIN workspace_members wm
        ON w.id = wm.workspace_id
      WHERE wm.user_id = :memberId
      """,
    )
    fun findByMemberId(memberId: UUID): Flow<WorkspaceEntity>
}
