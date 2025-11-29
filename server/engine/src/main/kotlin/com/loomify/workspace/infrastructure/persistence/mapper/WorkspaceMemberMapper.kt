package com.loomify.workspace.infrastructure.persistence.mapper

import com.loomify.workspace.domain.WorkspaceMember
import com.loomify.workspace.domain.WorkspaceMemberId
import com.loomify.workspace.infrastructure.persistence.entity.WorkspaceMemberEntity

/**
 * Converts a [WorkspaceMemberEntity] to a [WorkspaceMember] domain object.
 */
fun WorkspaceMemberEntity.toDomain(): WorkspaceMember = WorkspaceMember(
    id = WorkspaceMemberId(workspaceId = this.workspaceId, userId = this.userId),
    role = this.role,
)
