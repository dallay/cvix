package com.loomify.engine.workspace.application.update

import com.loomify.common.domain.Service
import com.loomify.common.domain.bus.event.EventBroadcaster
import com.loomify.common.domain.bus.event.EventPublisher
import com.loomify.engine.workspace.domain.WorkspaceFinderRepository
import com.loomify.engine.workspace.domain.WorkspaceId
import com.loomify.engine.workspace.domain.WorkspaceNotFoundException
import com.loomify.engine.workspace.domain.WorkspaceRepository
import com.loomify.engine.workspace.domain.event.WorkspaceUpdatedEvent
import java.util.*
import org.slf4j.LoggerFactory

/**
 * Service class responsible for updating workspaces.
 *
 * @property workspaceRepository The repository for workspace data.
 * @property workspaceFinderRepository The repository for finding workspaces.
 * @property eventPublisher The publisher for workspace update events.
 */
@Service
class WorkspaceUpdater(
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceFinderRepository: WorkspaceFinderRepository,
    eventPublisher: EventPublisher<WorkspaceUpdatedEvent>
) {
    private val eventBroadcaster = EventBroadcaster<WorkspaceUpdatedEvent>()

    init {
        this.eventBroadcaster.use(eventPublisher)
    }

    /**
     * Updates a workspace with the given id and name.
     * Throws a [WorkspaceNotFoundException] if the workspace is not found.
     *
     * @param teamId The id of the workspace to update.
     * @param name The new name of the workspace.
     * @param description The new description of the workspace.
     */
    suspend fun update(teamId: UUID, name: String, description: String?) {
        log.info("Updating workspace with id: {}", teamId)
        val workspaceId = WorkspaceId(teamId)
        val workspace = workspaceFinderRepository.findById(workspaceId)
            ?: throw WorkspaceNotFoundException("Workspace not found")
        workspace.update(name, description)
        workspaceRepository.update(workspace)
        val domainEvents = workspace.pullDomainEvents()
        domainEvents.filterIsInstance<WorkspaceUpdatedEvent>().forEach {
            eventBroadcaster.publish(it)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(WorkspaceUpdater::class.java)
    }
}
