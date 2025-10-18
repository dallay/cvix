package com.loomify.engine.workspace.application.create

import com.loomify.common.domain.bus.command.Command
import com.loomify.engine.AppConstants.UUID_PATTERN
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * Represents a command to create a workspace.
 *
 * @property id Unique identifier for the workspace.
 * @property name Name of the workspace.
 * @property description Optional description of the workspace.
 * @property ownerId Unique identifier of the owner of the workspace.
 * @property isDefault Whether this is the default workspace for the owner.
 */
data class CreateWorkspaceCommand(
    @field:NotBlank(message = "Workspace ID cannot be blank")
    @field:Pattern(
        regexp = UUID_PATTERN,
        message = "Workspace ID must be a valid UUID",
    )
    val id: String,
    @field:NotBlank(message = "Workspace name cannot be blank")
    val name: String,
    val description: String? = null,
    @field:NotBlank(message = "Owner ID cannot be blank")
    @field:Pattern(
        regexp = UUID_PATTERN,
        message = "Owner ID must be a valid UUID",
    )
    val ownerId: String,
    val isDefault: Boolean = false,
) : Command
