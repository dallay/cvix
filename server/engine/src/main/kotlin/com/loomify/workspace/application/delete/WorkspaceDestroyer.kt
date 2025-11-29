package com.loomify.workspace.application.delete

import com.loomify.common.domain.Service
import com.loomify.common.domain.bus.event.EventBroadcaster
import com.loomify.common.domain.bus.event.EventPublisher
import com.loomify.workspace.domain.WorkspaceId
import com.loomify.workspace.domain.WorkspaceRepository
import com.loomify.workspace.domain.event.WorkspaceDeletedEvent
import org.slf4j.LoggerFactory

/**
 * This service class is responsible for deleting workspaces.
 *
 * @property destroyer The repository that handles the deletion of workspaces.
 * @property eventPublisher The publisher that handles the broadcasting of workspace deletion events.
 */
@Service
class WorkspaceDestroyer(
    private val destroyer: WorkspaceRepository,
    eventPublisher: EventPublisher<WorkspaceDeletedEvent>
) {
    private val eventBroadcaster = EventBroadcaster<WorkspaceDeletedEvent>()

    init {
        this.eventBroadcaster.use(eventPublisher)
    }

    /**
     * Deletes a workspace with the given id.
     *
     * @param id The id of the workspace to be deleted.
     */
    suspend fun delete(id: WorkspaceId) {
        log.debug("Deleting workspace with id: {}", id)
        destroyer.delete(id)
        eventBroadcaster.publish(WorkspaceDeletedEvent(id.id.toString()))
    }

    companion object {
        private val log = LoggerFactory.getLogger(WorkspaceDestroyer::class.java)
    }
}
