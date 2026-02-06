package com.cvix.identity.domain.workspace.event

import com.cvix.common.domain.bus.event.BaseDomainEvent

/**
 * WorkspaceUpdatedEvent is a data class representing the event of a workspace being updated.
 * It extends BaseDomainEvent.
 *
 * @property id The unique identifier of the workspace that was updated.
 * @property workspaceName The new name of the workspace after the update.
 * @property ownerId The unique identifier of the user who owns the workspace.
 */
data class WorkspaceUpdatedEvent(
    val id: String,
    val workspaceName: String,
    val ownerId: String
) : BaseDomainEvent()
