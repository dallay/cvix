package com.cvix.resume.application.delete

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandHandler
import org.slf4j.LoggerFactory

/**
 * Command handler for deleting resume documents.
 * Delegates to ResumeDestroyer which enforces authorization - only the owner can delete.
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
        log.debug("Deleting resume - id={}", command.id)
        destroyer.deleteResume(command.id, command.userId)
    }
    companion object {
        private val log = LoggerFactory.getLogger(DeleteResumeCommandHandler::class.java)
    }
}
