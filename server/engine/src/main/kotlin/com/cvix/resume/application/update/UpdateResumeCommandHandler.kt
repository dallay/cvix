package com.cvix.resume.application.update

import com.cvix.common.domain.bus.command.CommandHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Command handler for updating existing resume documents.
 * Implements optimistic locking via updatedAt timestamp check.
 */
@Service
class UpdateResumeCommandHandler(
    private val resumeUpdater: ResumeUpdater,
) : CommandHandler<UpdateResumeCommand> {
    private val logger = LoggerFactory.getLogger(UpdateResumeCommandHandler::class.java)

    /**
     * Handles the update resume command.
     * @param command The command containing resume data and update metadata
     */
    override suspend fun handle(command: UpdateResumeCommand) {
        logger.info("Updating resume - id={}, userId={}", command.id, command.userId)

        resumeUpdater.update(
            command.id, command.userId, command.workspaceId,
            command.title ?: command.content.basics.name.value, command.content, command.updatedBy,
        )
    }
}
