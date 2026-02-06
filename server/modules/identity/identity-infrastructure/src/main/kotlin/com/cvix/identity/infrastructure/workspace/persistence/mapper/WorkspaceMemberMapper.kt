package com.cvix.identity.infrastructure.workspace.persistence.mapper

import com.cvix.identity.domain.workspace.WorkspaceMember
import com.cvix.identity.domain.workspace.WorkspaceMemberId
import com.cvix.identity.infrastructure.workspace.persistence.entity.WorkspaceMemberEntity

/**
 * Converts a [WorkspaceMemberEntity] to a [WorkspaceMember] domain object.
 */
fun WorkspaceMemberEntity.toDomain(): WorkspaceMember = WorkspaceMember(
    id = WorkspaceMemberId(workspaceId = this.workspaceId, userId = this.userId),
    role = this.role,
)
