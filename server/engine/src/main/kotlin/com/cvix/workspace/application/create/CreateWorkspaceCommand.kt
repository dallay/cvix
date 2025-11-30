package com.cvix.workspace.application.create

import com.cvix.common.domain.bus.command.Command
import java.util.*

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
    val id: UUID,
    val name: String,
    val description: String? = null,
    val ownerId: UUID,
    val isDefault: Boolean = false,
) : Command
