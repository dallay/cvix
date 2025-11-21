package com.loomify.resume.application.update

import com.loomify.common.domain.Service
import com.loomify.common.domain.bus.event.EventBroadcaster
import com.loomify.common.domain.bus.event.EventPublisher
import com.loomify.resume.domain.Resume
import com.loomify.resume.domain.ResumeRepository
import com.loomify.resume.domain.event.ResumeUpdatedEvent
import com.loomify.resume.domain.exception.ResumeNotFoundException
import java.util.*
import org.slf4j.LoggerFactory

/**
 * Service responsible for updating resume documents. Publishes a [ResumeUpdatedEvent] upon successful update.
 * @created 19/11/25
 */
@Service
class ResumeUpdater(
    private val resumeRepository: ResumeRepository,
    eventPublisher: EventPublisher<ResumeUpdatedEvent>
) {
    private val eventBroadcaster = EventBroadcaster<ResumeUpdatedEvent>()
    init {
        this.eventBroadcaster.use(eventPublisher)
    }
/**
     * Updates a resume document and publishes a [ResumeUpdatedEvent].
     *
     * @param id The ID of the resume document
     * @param userId The ID of the user updating the resume
     * @param workspaceId The ID of the workspace where the resume is located
     * @param title The new title of the resume
     * @param content The updated resume data following JSON Resume schema
     * @param createdBy The username or email of the updater
     *
     * @throws ResumeNotFoundException if the resume does not exist
     */
    suspend fun update(
        id: UUID,
        userId: UUID,
        workspaceId: UUID,
        title: String,
        content: Resume,
        createdBy: String
    ) {
        log.debug(
            "Updating resume document - id={}, userId={}, workspaceId={}, title={}",
            id, userId, workspaceId, title,
        )
        resumeRepository.findById(id, userId)?.let { existing ->
            val updatedDocument = existing.update(
                title = title,
                newContent = content,
                updatedBy = createdBy,
            )
            val savedDocument = resumeRepository.save(updatedDocument)
            log.debug(
                "Resume document updated successfully - id={}, title={}",
                savedDocument.id, savedDocument.title,
            )
            eventBroadcaster.publish(
                ResumeUpdatedEvent(
                    resumeId = savedDocument.id.value.toString(),
                    userId = userId.toString(),
                    workspaceId = workspaceId.toString(),
                ),
            )
        } ?: run {
            log.warn("Resume document not found for update - id={}, userId={}", id, userId)
            throw ResumeNotFoundException("Resume not found: $id")
        }
    }
    companion object {
        private val log = LoggerFactory.getLogger(ResumeUpdater::class.java)
    }
}
