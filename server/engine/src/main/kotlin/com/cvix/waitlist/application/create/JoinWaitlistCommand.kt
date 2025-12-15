package com.cvix.waitlist.application.create

import com.cvix.common.domain.bus.command.Command
import java.util.*

/**
 * Command to join the waitlist.
 *
 * @property id The unique identifier for the waitlist entry (generated client-side or server-side).
 * @property email The user's email address.
 * @property source The source from where the user is joining (e.g., "landing-hero", "landing-cta").
 * @property language The user's preferred language code (e.g., "en", "es").
 * @property ipAddress The user's IP address (will be hashed for privacy).
 * @property metadata Additional optional metadata (user agent, referrer, etc.).
 */
data class JoinWaitlistCommand(
    val id: UUID,
    val email: String,
    val source: String,
    val language: String,
    val ipAddress: String? = null,
    val metadata: Map<String, Any>? = null
) : Command
