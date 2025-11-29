package com.cvix.workspace.infrastructure.persistence.mapper

import com.cvix.workspace.domain.WorkspaceMember
import com.cvix.workspace.domain.WorkspaceMemberId
import com.cvix.workspace.infrastructure.persistence.entity.WorkspaceMemberEntity

/**
 * Converts a [WorkspaceMemberEntity] to a [WorkspaceMember] domain object.
 */
fun WorkspaceMemberEntity.toDomain(): WorkspaceMember = WorkspaceMember(
    id = WorkspaceMemberId(workspaceId = this.workspaceId, userId = this.userId),
    role = this.role,
)
