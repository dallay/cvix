package com.loomify.workspace.application.find.member

import com.loomify.common.domain.bus.query.Query
import com.loomify.workspace.application.WorkspaceResponses
import java.util.*

/**
 * This class represents a query to find all workspaces.
 *
 * @property userId The unique identifier of the user.
 */
data class AllWorkspaceByMemberQuery(
    val userId: UUID
) : Query<WorkspaceResponses>
