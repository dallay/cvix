package com.loomify.engine.workspace.application.find

import com.loomify.common.domain.bus.query.Query
import com.loomify.engine.AppConstants.UUID_PATTERN
import com.loomify.engine.workspace.application.WorkspaceResponse
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * Represents a query to find a workspace by its ID.
 *
 * @property id The ID of the workspace to find.
 */
data class FindWorkspaceQuery(
    @field:NotBlank(message = "Workspace ID cannot be blank")
    @field:Pattern(
        regexp = UUID_PATTERN,
        message = "Workspace ID must be a valid UUID",
    )
    val id: String
) : Query<WorkspaceResponse> {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FindWorkspaceQuery) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
