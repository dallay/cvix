package com.cvix.resume.application.create

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.event.EventBroadcaster
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.resume.domain.Resume
import com.cvix.resume.domain.ResumeDocument
import com.cvix.resume.domain.ResumeRepository
import com.cvix.resume.domain.event.ResumeCreatedEvent
import java.util.*
import org.slf4j.LoggerFactory

/**
 * Service responsible for creating resume documents. Publishes a [ResumeCreatedEvent] upon successful creation.
 * @created 19/11/25
 */
@Service
class ResumeCreator(
    private val resumeRepository: ResumeRepository,
    eventPublisher: EventPublisher<ResumeCreatedEvent>
) {
    private val eventBroadcaster = EventBroadcaster<ResumeCreatedEvent>()

    init {
        this.eventBroadcaster.use(eventPublisher)
    }

    /**
     * Creates a resume document and publishes a [ResumeCreatedEvent].
     *
     * @param id The ID of the resume document
     * @param userId The ID of the user creating the resume
     * @param workspaceId The ID of the workspace where the resume will be created
     * @param title The title of the resume
     * @param content The resume data following JSON Resume schema
     * @param createdBy The username or email of the creator
     *
     * @throws Exception if there is an error during creation
     */
    suspend fun create(
        id: UUID,
        userId: UUID,
        workspaceId: UUID,
        title: String,
        content: Resume,
        createdBy: String
    ) {
        log.debug(
            "Creating resume document - id={}, userId={}, workspaceId={}, title={}",
            id, userId, workspaceId, title,
        )
        // Create new resume document from domain factory
        val document = ResumeDocument.create(
            id = id,
            userId = userId,
            workspaceId = workspaceId,
            title = title,
            content = content,
            createdBy = createdBy,
        )
        val savedDocument = resumeRepository.save(document)
        log.debug(
            "Resume document created with id={}, title={}",
            savedDocument.id,
            savedDocument.title,
        )

        val domainEvents = savedDocument.pullDomainEvents()
        domainEvents.filterIsInstance<ResumeCreatedEvent>().forEach { eventBroadcaster.publish(it) }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ResumeCreator::class.java)
    }
}
