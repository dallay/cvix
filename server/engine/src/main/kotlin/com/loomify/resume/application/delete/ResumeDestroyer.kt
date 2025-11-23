package com.loomify.resume.application.delete

import com.loomify.common.domain.Service
import com.loomify.common.domain.bus.event.EventBroadcaster
import com.loomify.common.domain.bus.event.EventPublisher
import com.loomify.resume.domain.ResumeRepository
import com.loomify.resume.domain.event.ResumeDeletedEvent
import com.loomify.resume.domain.exception.ResumeAccessDeniedException
import java.util.UUID
import org.slf4j.LoggerFactory

/**
 * Service responsible for deleting resume documents from the system.
 *
 * This class interacts with the `ResumeRepository` to remove resume records
 * and emits a `ResumeDeletedEvent` upon successful deletion. It ensures that
 * the deletion process is encapsulated within the service layer, adhering to
 * the application's domain logic.
 *
 * Dependencies:
 * - `ResumeRepository`: Repository interface for accessing and managing resume data.
 * - `EventPublisher<ResumeDeletedEvent>`: Publishes domain events related to resume deletion.
 *
 * Annotations:
 * - `@Service`: Marks this class as a Spring service component, making it eligible
 *   for Spring's component scanning and dependency injection.
 *
 * @created 20/11/25
 */
@Service
class ResumeDestroyer(
    private val resumeRepository: ResumeRepository,
    eventPublisher: EventPublisher<ResumeDeletedEvent>
) {
    private val eventBroadcaster = EventBroadcaster<ResumeDeletedEvent>()

    init {
        this.eventBroadcaster.use(eventPublisher)
    }

    /**
     * Deletes a resume by its ID and user ID.
     *
     * @param id The ID of the resume to delete
     * @param userId The ID of the user requesting the deletion
     */
    suspend fun deleteResume(id: UUID, userId: UUID) {
        log.debug("Deleting resume - id={}, userId={}", id, userId)
        val exists = resumeRepository.existsById(id, userId)
        if (!exists) {
            throw com.loomify.resume.domain.exception.ResumeNotFoundException("Resume not found: $id")
        }
        val rowsDeleted = resumeRepository.deleteIfAuthorized(id, userId)
        if (rowsDeleted == 0L) {
            throw ResumeAccessDeniedException("Resume access denied: $id")
        }
        eventBroadcaster.publish(
            ResumeDeletedEvent(
                resumeId = id,
                userId = userId,
            ),
        )
        log.debug("Resume deleted successfully - id={}", id)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ResumeDestroyer::class.java)
    }
}
