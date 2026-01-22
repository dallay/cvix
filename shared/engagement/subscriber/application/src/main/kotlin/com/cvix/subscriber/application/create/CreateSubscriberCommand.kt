package com.cvix.subscriber.application.create

import com.cvix.common.domain.bus.command.Command
import com.cvix.subscriber.domain.Attributes
import java.util.*

/**
 * Command object for creating a new subscriber.
 *
 * Encapsulates all data required to register a subscriber, including optional IP address and attributes.
 *
 * @property id Unique identifier for the subscriber.
 * @property email Email address of the subscriber.
 * @property source Source of the subscription (e.g., web, referral).
 * @property language Preferred language of the subscriber.
 * @property ipAddress Optional IP address of the subscriber.
 * @property attributes Optional additional attributes for the subscriber, defaults to an empty [Attributes] instance.
 * @property workspaceId Identifier of the workspace associated with the subscriber.
 */
data class CreateSubscriberCommand(
    val id: UUID,
    val email: String,
    val source: String,
    val language: String,
    val ipAddress: String? = null,
    val attributes: Attributes? = Attributes(),
    val workspaceId: UUID
) : Command
