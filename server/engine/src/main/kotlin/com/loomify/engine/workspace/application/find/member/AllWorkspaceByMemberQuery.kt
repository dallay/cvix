package com.loomify.engine.workspace.application.find.member

import com.loomify.common.domain.bus.query.Query
import com.loomify.engine.AppConstants.UUID_PATTERN
import com.loomify.engine.workspace.application.WorkspaceResponses
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * This class represents a query to find all workspaces.
 *
 * @property userId The unique identifier of the user.
 */
data class AllWorkspaceByMemberQuery(
    @field:NotBlank(message = "User ID cannot be blank")
    @field:Pattern(
        regexp = UUID_PATTERN,
        message = "User ID must be a valid UUID",
    )
    val userId: String
) : Query<WorkspaceResponses>
