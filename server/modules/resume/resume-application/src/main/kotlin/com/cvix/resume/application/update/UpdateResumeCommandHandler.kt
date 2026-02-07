package com.cvix.resume.application.update

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandWithResultHandler
import com.cvix.resume.application.ResumeDocumentResponse
import org.slf4j.LoggerFactory

/**
 * Command handler for updating existing resume documents.
 * Implements optimistic locking via updatedAt timestamp check.
 *
 * Returns [ResumeDocumentResponse] containing the updated resume with server-generated
 * timestamps, allowing clients to track the resume ID and display "last saved" indicators.
 */
@Service
class UpdateResumeCommandHandler(
    private val resumeUpdater: ResumeUpdater,
) : CommandWithResultHandler<UpdateResumeCommand, ResumeDocumentResponse> {
    private val logger = LoggerFactory.getLogger(UpdateResumeCommandHandler::class.java)

    /**
     * Handles the update resume command.
     * @param command The command containing resume data and update metadata
     * @return [ResumeDocumentResponse] with the updated resume document
     */
    override suspend fun handle(command: UpdateResumeCommand): ResumeDocumentResponse {
        logger.info("Updating resume - id={}, userId={}", command.id, command.userId)

        val savedDocument = resumeUpdater.update(
            command.id, command.userId, command.workspaceId,
            command.title ?: command.content.basics.name.value, command.content, command.updatedBy,
            command.expectedUpdatedAt,
        )
        return ResumeDocumentResponse.from(savedDocument)
    }
}
