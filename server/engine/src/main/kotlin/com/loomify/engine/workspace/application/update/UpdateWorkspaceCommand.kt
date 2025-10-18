package com.loomify.engine.workspace.application.update

import com.loomify.common.domain.bus.command.Command
import com.loomify.engine.AppConstants.UUID_PATTERN
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * Represents a command to update a workspace.
 * @property id The unique identifier of the workspace.
 * @property name The new name of the workspace.
 * @property description An optional description of the workspace.
 */
data class UpdateWorkspaceCommand(
    @field:NotBlank(message = "Workspace ID cannot be blank")
    @field:Pattern(
        regexp = UUID_PATTERN,
        message = "Workspace ID must be a valid UUID",
    )
    val id: String,
    @field:NotBlank(message = "Workspace name cannot be blank")
    val name: String,
    val description: String?
) :
    Command
