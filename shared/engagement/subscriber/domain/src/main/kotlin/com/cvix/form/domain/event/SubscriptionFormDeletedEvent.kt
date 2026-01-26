package com.cvix.form.domain.event

import com.cvix.common.domain.bus.event.BaseDomainEvent
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.form.domain.SubscriptionFormId
import java.time.Instant

/**
 * Domain event emitted when a subscription form is deleted.
 *
 * @property formId Identifier of the deleted form.
 * @property workspaceId Workspace where the form belonged.
 * @property deletedAt Timestamp when the deletion occurred.
 * @property deletedBy The actor that performed the deletion.
 */
data class SubscriptionFormDeletedEvent(
    val formId: SubscriptionFormId,
    val workspaceId: WorkspaceId,
    val deletedAt: Instant = Instant.now(),
    val deletedBy: String = "system",
) : BaseDomainEvent()
