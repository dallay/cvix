package com.cvix.identity.domain.workspace

import com.cvix.common.domain.model.BaseEntity

data class WorkspaceMember(
    override val id: WorkspaceMemberId,
    val role: WorkspaceRole = WorkspaceRole.EDITOR,
) : BaseEntity<WorkspaceMemberId>()
