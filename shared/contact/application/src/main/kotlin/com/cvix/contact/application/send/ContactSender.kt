package com.cvix.contact.application.send

import com.cvix.common.domain.Service
import com.cvix.contact.domain.CaptchaValidationException
import com.cvix.contact.domain.CaptchaValidator
import com.cvix.contact.domain.ContactData
import com.cvix.contact.domain.ContactNotificationException
import com.cvix.contact.domain.ContactNotifier
import java.util.*
import org.slf4j.LoggerFactory

/**
 * Service responsible for validating and sending contact form submissions.
 *
 * This service orchestrates the following steps:
 * 1. Validates the hCaptcha token using the CaptchaValidator port
 * 2. Sends the contact data using the ContactNotifier port
 *
 * Following hexagonal architecture, this application service depends only on
 * domain interfaces (ports), not on infrastructure implementations.
 *
 * @property captchaValidator Port for CAPTCHA validation (infrastructure provides implementation).
 * @property contactNotifier Port for contact notifications (infrastructure provides implementation).
 */
@Service
class ContactSender(
    private val captchaValidator: CaptchaValidator,
    private val contactNotifier: ContactNotifier,
) {

    /**
     * Validates hCaptcha and sends the contact form data through the notification port.
     *
     * @param id Unique identifier for this submission.
     * @param name Sender's full name.
     * @param email Sender's email address.
     * @param subject Message subject.
     * @param message Message content.
     * @param hcaptchaToken hCaptcha verification token.
     * @param ipAddress Sender's IP address.
     * @throws CaptchaValidationException if hCaptcha validation fails.
     * @throws ContactNotificationException if sending notification fails.
     */
    @Suppress("LongParameterList")
    suspend fun send(
        id: UUID,
        name: String,
        email: String,
        subject: String,
        message: String,
        hcaptchaToken: String,
        ipAddress: String,
    ) {
        logger.info("Processing contact form submission ID: {}", id)

        // Step 1: Validate hCaptcha token through the port
        val isValid = captchaValidator.verify(hcaptchaToken, ipAddress)
        if (!isValid) {
            logger.warn("hCaptcha validation failed for submission ID: {}, IP: {}", id, ipAddress)
            throw CaptchaValidationException("Invalid hCaptcha token")
        }

        logger.info("hCaptcha validation successful for submission ID: {}", id)

        // Step 2: Send notification through the port
        val contactData = ContactData(
            name = name,
            email = email,
            subject = subject,
            message = message,
        )

        try {
            contactNotifier.notify(contactData)
            logger.info("Successfully sent contact notification for ID: {}", id)
        } catch (e: Exception) {
            logger.error("Failed to send contact notification for ID: {}", id, e)
            throw ContactNotificationException("Failed to send contact notification", e)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ContactSender::class.java)
    }
}
