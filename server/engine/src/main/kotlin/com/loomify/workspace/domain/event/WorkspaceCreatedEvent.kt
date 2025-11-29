package com.loomify.workspace.domain.event

import com.loomify.common.domain.bus.event.BaseDomainEvent

/**
 * WorkspaceCreatedEvent is a data class representing the event of a workspace being created.
 * It extends BaseDomainEvent.
 *
 * @property id The unique identifier of the workspace that was created.
 * @property name The name of the workspace that was created.
 * @property ownerId The unique identifier of the user who created the workspace.
 */
data class WorkspaceCreatedEvent(
    val id: String,
    val name: String,
    val ownerId: String
) :
    BaseDomainEvent()
