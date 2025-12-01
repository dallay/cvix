package com.cvix.resume.application.create

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandHandler
import org.slf4j.LoggerFactory

/**
 * Command handler for creating new resume documents.
 * Orchestrates the resume creation process following CQRS pattern.
 */
@Service
class CreateResumeCommandHandler(
    private val resumeCreator: ResumeCreator,
) : CommandHandler<CreateResumeCommand> {

    /**
     * Handles the create resume command.
     * @param command The command containing resume data and metadata
     */
    override suspend fun handle(command: CreateResumeCommand) {
        log.info(
            "Creating resume - userId={}, workspaceId={}, createdBy={}",
            command.userId,
            command.workspaceId,
            command.createdBy,
        )
        resumeCreator.create(
            id = command.id,
            userId = command.userId,
            workspaceId = command.workspaceId,
            title = command.title ?: command.content.basics.name.value,
            content = command.content,
            createdBy = command.createdBy,
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(CreateResumeCommandHandler::class.java)
    }
}
