package com.cvix.contact.application.send

import com.cvix.common.domain.bus.command.Command
import java.util.*

/**
 * Command to send a contact form submission.
 *
 * @property id The unique identifier for the contact submission (generated server-side).
 * @property name The sender's full name.
 * @property email The sender's email address.
 * @property subject The subject of the message.
 * @property message The message content.
 * @property hcaptchaToken The hCaptcha verification token from the client.
 * @property ipAddress The sender's IP address (for rate limiting and abuse prevention).
 * @property metadata Additional optional metadata (user agent, referrer, etc.).
 */
data class SendContactCommand(
    val id: UUID,
    val name: String,
    val email: String,
    val subject: String,
    val message: String,
    val hcaptchaToken: String,
    val ipAddress: String,
    val metadata: Map<String, String> = emptyMap()
) : Command
