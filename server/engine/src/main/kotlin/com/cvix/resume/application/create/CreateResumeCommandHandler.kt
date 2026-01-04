package com.cvix.resume.application.create

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandWithResultHandler
import com.cvix.resume.application.ResumeDocumentResponse
import org.slf4j.LoggerFactory

/**
 * Command handler for creating new resume documents.
 * Orchestrates the resume creation process following CQRS pattern.
 *
 * Returns [ResumeDocumentResponse] containing the created resume with server-generated
 * timestamps, allowing clients to track the resume ID and display "last saved" indicators.
 */
@Service
class CreateResumeCommandHandler(
    private val resumeCreator: ResumeCreator,
) : CommandWithResultHandler<CreateResumeCommand, ResumeDocumentResponse> {

    /**
     * Handles the create resume command.
     * @param command The command containing resume data and metadata
     * @return [ResumeDocumentResponse] with the created resume document
     */
    override suspend fun handle(command: CreateResumeCommand): ResumeDocumentResponse {
        log.info(
            "Creating resume - userId={}, workspaceId={}, createdBy={}",
            command.userId,
            command.workspaceId,
            command.createdBy,
        )
        val savedDocument = resumeCreator.create(
            id = command.id,
            userId = command.userId,
            workspaceId = command.workspaceId,
            title = command.title ?: command.content.basics.name.value,
            content = command.content,
            createdBy = command.createdBy,
        )
        return ResumeDocumentResponse.from(savedDocument)
    }

    companion object {
        private val log = LoggerFactory.getLogger(CreateResumeCommandHandler::class.java)
    }
}
