package com.cvix.form.domain.event

import com.cvix.common.domain.bus.event.BaseDomainEvent
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.SYSTEM_USER
import com.cvix.form.domain.SubscriptionForm
import com.cvix.form.domain.SubscriptionFormId
import java.time.Instant

/**
 * Domain event emitted when a [SubscriptionForm] is created.
 *
 * @property formId Identifier of the created form.
 * @property workspaceId Workspace where the form was created.
 * @property createdAt Timestamp when the form was created.
 * @property createdBy The actor that created the form.
 */
data class SubscriptionFormCreatedEvent(
    val formId: SubscriptionFormId,
    val workspaceId: WorkspaceId,
    val createdAt: Instant = Instant.now(),
    val createdBy: String = SYSTEM_USER,
    val payload: SubscriptionForm,
): BaseDomainEvent()
