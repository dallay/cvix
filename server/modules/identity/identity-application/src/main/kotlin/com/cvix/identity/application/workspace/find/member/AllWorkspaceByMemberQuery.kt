package com.cvix.identity.application.workspace.find.member

import com.cvix.common.domain.bus.query.Query
import com.cvix.identity.application.workspace.WorkspaceResponses
import java.util.UUID

/**
 * This class represents a query to find all workspaces.
 *
 * @property userId The unique identifier of the user.
 */
data class AllWorkspaceByMemberQuery(
    val userId: UUID
) : Query<WorkspaceResponses>
