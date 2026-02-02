package com.cvix.form.domain.event

import com.cvix.common.domain.SYSTEM_USER_UUID
import com.cvix.common.domain.bus.event.BaseDomainEvent
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.form.domain.SubscriptionFormId
import java.time.Instant
import java.util.UUID

/**
 * Value class representing an actor identifier (user or system).
 *
 * @property value The underlying UUID value representing the actor.
 */
@JvmInline
value class ActorId(val value: UUID) {
    companion object {
        /**
         * Creates an ActorId from a string UUID.
         *
         * @param id The UUID string representation.
         * @return An ActorId wrapping the parsed UUID.
         */
        fun from(id: String): ActorId = ActorId(UUID.fromString(id))
    }
}

/**
 * Lightweight summary of a subscription form for event payloads.
 *
 * @property formName The name of the form.
 * @property confirmationRequired Whether email confirmation is required.
 */
data class SubscriptionFormSummary(
    val formName: String,
    val confirmationRequired: Boolean,
)

/**
 * Domain event emitted when a subscription form is created.
 *
 * @property formId Identifier of the created form.
 * @property workspaceId Workspace where the form was created.
 * @property createdAt Timestamp when the form was created.
 * @property createdBy The actor that created the form.
 * @property summary Lightweight summary of the created form.
 */
data class SubscriptionFormCreatedEvent(
    val formId: SubscriptionFormId,
    val workspaceId: WorkspaceId,
    val createdAt: Instant = Instant.now(),
    val createdBy: ActorId = ActorId(SYSTEM_USER_UUID),
    val summary: SubscriptionFormSummary,
) : BaseDomainEvent()
