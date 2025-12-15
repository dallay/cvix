package com.cvix.waitlist.application.create

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandHandler
import com.cvix.common.domain.vo.email.Email
import com.cvix.waitlist.domain.Language
import com.cvix.waitlist.domain.WaitlistEntryId
import com.cvix.waitlist.domain.WaitlistSource
import org.slf4j.LoggerFactory

/**
 * Command handler for joining the waitlist.
 *
 * This handler receives a JoinWaitlistCommand and delegates the business logic
 * to the WaitlistJoiner service.
 *
 * @property waitlistJoiner The service responsible for adding users to the waitlist.
 */
@Service
class JoinWaitlistCommandHandler(
    private val waitlistJoiner: WaitlistJoiner,
) : CommandHandler<JoinWaitlistCommand> {

    /**
     * Handles the command to join the waitlist.
     *
     * @param command The command containing the user's information.
     * @throws com.cvix.waitlist.domain.EmailAlreadyExistsException if the email already exists.
     * @throws IllegalArgumentException if the email format is invalid.
     */
    override suspend fun handle(command: JoinWaitlistCommand) {
        logger.info("Handling JoinWaitlistCommand for email from source: {}", command.source)

        try {
            // Parse and validate email
            val email = Email.of(command.email)
                ?: throw IllegalArgumentException("Invalid email format: ${command.email}")

            // Parse source and language
            val source = WaitlistSource.fromString(command.source)
            val language = Language.fromString(command.language)

            // Create entry ID
            val entryId = WaitlistEntryId(command.id)

            // Call service to join waitlist
            waitlistJoiner.join(
                id = entryId,
                email = email,
                source = source,
                language = language,
                ipAddress = command.ipAddress,
                metadata = command.metadata,
            )

            logger.info("Successfully processed JoinWaitlistCommand with ID: {}", command.id)
        } catch (e: Exception) {
            logger.error("Failed to process JoinWaitlistCommand with ID: {}", command.id, e)
            throw e
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JoinWaitlistCommandHandler::class.java)
    }
}
