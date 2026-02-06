package com.cvix.identity.application.workspace.create

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.event.EventBroadcaster
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.identity.domain.workspace.Workspace
import com.cvix.identity.domain.workspace.WorkspaceFinderRepository
import com.cvix.identity.domain.workspace.WorkspaceMetrics
import com.cvix.identity.domain.workspace.WorkspaceRepository
import com.cvix.identity.domain.workspace.event.WorkspaceCreatedEvent
import org.slf4j.LoggerFactory

/**
 * WorkspaceCreator is a service class responsible for creating workspaces.
 * It handles idempotency for default workspaces by checking for existing workspaces
 * before creation, and publishes a WorkspaceCreatedEvent for each created workspace.
 *
 * @property workspaceRepository The repository used to persist workspaces.
 * @property workspaceFinderRepository The repository used to query existing workspaces.
 * @property workspaceMetrics The metrics abstraction for tracking workspace operations.
 * @property eventPublisher The EventPublisher used to publish WorkspaceCreatedEvents.
 */
@Service
class WorkspaceCreator(
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceFinderRepository: WorkspaceFinderRepository,
    private val workspaceMetrics: WorkspaceMetrics,
    eventPublisher: EventPublisher<WorkspaceCreatedEvent>,
) {
    private val eventBroadcaster = EventBroadcaster<WorkspaceCreatedEvent>()

    init {
        this.eventBroadcaster.use(eventPublisher)
    }

    /**
     * Creates a workspace using the WorkspaceRepository and publishes a
     * [WorkspaceCreatedEvent] for the created workspace.
     *
     * For default workspaces, this method ensures idempotency by checking if
     * the user already has an existing workspace. If so, creation is skipped
     * to prevent race conditions.
     *
     * @param workspace The workspace to be created.
     * @return true if the workspace was created, false if creation was skipped (duplicate default workspace)
     */
    suspend fun create(workspace: Workspace): Boolean {
        log.debug("Creating workspace with id: {}", workspace.id)

        // For default workspaces, check if one already exists to prevent race conditions
        if (workspace.isDefault) {
            val existingWorkspaces = workspaceFinderRepository.findByOwnerId(workspace.ownerId)
            if (existingWorkspaces.isNotEmpty()) {
                workspaceMetrics.incrementDuplicateDefaultIgnored()
                log.info(
                    "Default workspace already exists for user {} (found {} workspace(s)), skipping creation",
                    workspace.ownerId.value,
                    existingWorkspaces.size,
                )
                return false
            }
        }

        workspaceRepository.create(workspace)
        val domainEvents = workspace.pullDomainEvents()
        domainEvents.filterIsInstance<WorkspaceCreatedEvent>().forEach {
            eventBroadcaster.publish(it)
        }
        return true
    }

    companion object {
        private val log = LoggerFactory.getLogger(WorkspaceCreator::class.java)
    }
}
