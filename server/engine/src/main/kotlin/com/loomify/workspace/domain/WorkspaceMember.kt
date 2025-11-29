package com.loomify.workspace.domain

import com.loomify.common.domain.BaseEntity

data class WorkspaceMember(
    override val id: WorkspaceMemberId,
    val role: WorkspaceRole = WorkspaceRole.EDITOR,
) : BaseEntity<WorkspaceMemberId>()
