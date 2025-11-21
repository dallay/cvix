package com.loomify.resume.application.delete

import com.loomify.common.domain.Service
import com.loomify.common.domain.bus.command.CommandHandler
import org.slf4j.LoggerFactory

/**
 * Command handler for deleting resume documents.
 * Enforces authorization - only the owner can delete.
 */
@Service
class DeleteResumeCommandHandler(
    private val destroyer: ResumeDestroyer,
) : CommandHandler<DeleteResumeCommand> {

    /**
     * Handles the delete resume command.
     * @param command The command containing resume ID and user ID
     */
    override suspend fun handle(command: DeleteResumeCommand) {
        log.debug("Deleting resume - id={}, userId={}", command.id, command.userId)
        destroyer.deleteResume(command.id, command.userId)
    }
    companion object {
        private val log = LoggerFactory.getLogger(DeleteResumeCommandHandler::class.java)
    }
}
