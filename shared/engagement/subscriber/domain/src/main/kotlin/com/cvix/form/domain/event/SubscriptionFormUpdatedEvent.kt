package com.cvix.form.domain.event

import com.cvix.common.domain.bus.event.BaseDomainEvent
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.form.domain.SubscriptionForm
import com.cvix.form.domain.SubscriptionFormId
import java.time.Instant

/**
 * Domain event emitted when a [SubscriptionForm] is updated.
 *
 * @property formId Identifier of the updated form.
 * @property workspaceId Workspace where the form belongs.
 * @property updatedAt Timestamp when the update occurred.
 * @property updatedBy The actor that performed the update.
 * @property payload The updated [SubscriptionForm] state.
 */
data class SubscriptionFormUpdatedEvent(
    val formId: SubscriptionFormId,
    val workspaceId: WorkspaceId,
    val updatedAt: Instant = Instant.now(),
    val updatedBy: String = "system",
    val payload: SubscriptionForm,
) : BaseDomainEvent()
