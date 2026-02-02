package com.cvix.common.domain.outbox

import java.time.Instant
import java.util.*

/**
 * Represents an entry in the outbox table for reliable event publishing.
 *
 * @property id Unique identifier of the outbox entry.
 * @property aggregateId Identifier of the aggregate that emitted the event.
 * @property aggregateType Type of the aggregate (e.g., "SubscriptionForm").
 * @property eventType Type of the event (e.g., "SubscriptionFormDeletedEvent").
 * @property payload JSON representation of the event data.
 * @property occurredAt Timestamp when the event occurred.
 * @property processedAt Timestamp when the event was successfully processed/published.
 */
data class OutboxEntry(
    val id: UUID = UUID.randomUUID(),
    val aggregateId: String,
    val aggregateType: String,
    val eventType: String,
    val payload: String,
    val occurredAt: Instant = Instant.now(),
    val processedAt: Instant? = null,
)
