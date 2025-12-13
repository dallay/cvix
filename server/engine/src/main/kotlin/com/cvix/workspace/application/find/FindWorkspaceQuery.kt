package com.cvix.workspace.application.find

import com.cvix.common.domain.bus.query.Query
import com.cvix.workspace.application.WorkspaceResponse
import java.util.UUID

/**
 * Represents a query to find a workspace by its ID.
 *
 * @property id The ID of the workspace to find.
 */
data class FindWorkspaceQuery(
    val id: UUID
) : Query<WorkspaceResponse> {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FindWorkspaceQuery) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
