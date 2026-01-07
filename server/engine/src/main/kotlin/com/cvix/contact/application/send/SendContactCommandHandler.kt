package com.cvix.contact.application.send

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandHandler
import org.slf4j.LoggerFactory

/**
 * Command handler for sending contact form submissions.
 *
 * This handler receives a SendContactCommand and delegates the business logic
 * to the ContactSender service.
 *
 * @property contactSender The service responsible for validating and sending contact messages.
 */
@Service
class SendContactCommandHandler(
    private val contactSender: ContactSender,
) : CommandHandler<SendContactCommand> {

    /**
     * Handles the command to send a contact form submission.
     *
     * @param command The command containing the contact form data.
     * @throws com.cvix.contact.domain.CaptchaValidationException if captcha validation fails.
     * @throws com.cvix.contact.domain.ContactNotificationException if sending notification fails.
     */
    override suspend fun handle(command: SendContactCommand) {
        logger.info(
            "Handling SendContactCommand with ID: {}, subject: {}",
            command.id,
            command.subject,
        )

        contactSender.send(
            id = command.id,
            name = command.name,
            email = command.email,
            subject = command.subject,
            message = command.message,
            hcaptchaToken = command.hcaptchaToken,
            ipAddress = command.ipAddress,
        )

        logger.info("Successfully processed SendContactCommand with ID: {}", command.id)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SendContactCommandHandler::class.java)
    }
}
