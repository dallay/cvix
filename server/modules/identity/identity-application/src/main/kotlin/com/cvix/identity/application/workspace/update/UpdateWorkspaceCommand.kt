package com.cvix.identity.application.workspace.update

import com.cvix.common.domain.bus.command.Command
import java.util.UUID

/**
 * Represents a command to update a workspace.
 * @property id The unique identifier of the workspace.
 * @property name The new name of the workspace.
 * @property description An optional description of the workspace.
 */
data class UpdateWorkspaceCommand(
    val id: UUID,
    val name: String,
    val description: String?
) : Command
