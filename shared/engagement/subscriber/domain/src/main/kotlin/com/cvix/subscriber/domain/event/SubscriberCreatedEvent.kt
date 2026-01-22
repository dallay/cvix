package com.cvix.subscriber.domain.event

import com.cvix.common.domain.bus.event.BaseDomainEvent
import com.cvix.common.domain.model.Language
import com.cvix.subscriber.domain.Attributes
import java.time.Instant

/**
 * Data class representing the event of a subscriber being created.
 *
 * @property id The unique identifier of the subscriber.
 * @property aggregateId The aggregate identifier of the subscriber.
 * @property email The email address of the subscriber.
 * @property status The status of the subscriber.
 * @property attributes The attributes of the subscriber.
 * @property source The source from which the subscriber was created.
 * @property language The preferred language of the subscriber.
 * @property ipAddress The IP address of the subscriber, if available.
 * @property workspaceId The identifier of the workspace associated with the subscriber.
 * @property createdAt The timestamp when the subscriber was created.
 */
data class SubscriberCreatedEvent(
    val id: String,
    val aggregateId: String,
    val email: String,
    val status: String,
    val attributes: Attributes?,
    val source: String,
    val language: Language,
    val ipAddress: String? = null,
    val workspaceId: String,
    val createdAt: Instant,
) : BaseDomainEvent()
