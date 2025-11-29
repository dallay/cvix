package com.cvix.workspace.application.delete

import com.cvix.common.domain.bus.command.Command
import java.util.*

/**
 * Represents a command to delete a workspace.
 *
 * @property id Unique identifier for the workspace.
 */
data class DeleteWorkspaceCommand(
    val id: UUID
) : Command
