package com.cvix.form.application.delete

import com.cvix.common.domain.SYSTEM_USER
import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.event.EventBroadcaster
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.outbox.OutboxEntry
import com.cvix.common.domain.outbox.OutboxRepository
import com.cvix.form.domain.SubscriptionFormFinderRepository
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormRepository
import com.cvix.form.domain.event.SubscriptionFormDeletedEvent
import java.time.Instant
import java.util.*
import org.slf4j.LoggerFactory

@Service
class FormDestroyer(
    private val formRepository: SubscriptionFormRepository,
    private val formFinder: SubscriptionFormFinderRepository,
    initialEventPublisher: EventPublisher<SubscriptionFormDeletedEvent>,
    private val outboxRepository: OutboxRepository,
) {
    private val eventPublisher = EventBroadcaster<SubscriptionFormDeletedEvent>()

    init {
        this.eventPublisher.use(initialEventPublisher)
    }

    suspend fun delete(workspaceId: WorkspaceId, subscriptionFormId: SubscriptionFormId) {
        log.info("Deleting subscription form with ID: $subscriptionFormId from workspace: $workspaceId")
        val form = formFinder.findByFormIdAndWorkspaceId(subscriptionFormId, workspaceId)
            ?: run {
                log.warn("Subscription form with ID: $subscriptionFormId not found in workspace: $workspaceId")
                return
            }

        // Delete the form, then persist an outbox entry atomically so the event can be
        // published reliably by a separate dispatcher after the transaction commits.
        formRepository.delete(form.id)

        val event = SubscriptionFormDeletedEvent(
            formId = form.id,
            workspaceId = workspaceId,
            deletedAt = Instant.now(),
            deletedBy = SYSTEM_USER,
        )

        val outbox = OutboxEntry(
            id = UUID.randomUUID(),
            aggregateType = "SubscriptionForm",
            aggregateId = form.id.value.toString(),
            eventType = event::class.simpleName ?: "SubscriptionFormDeletedEvent",
            payload = serializeEvent(event),
            occurredAt = Instant.now(),
        )

        outboxRepository.save(outbox)

        log.info(
            "Deleted subscription form with ID: $subscriptionFormId " +
                "from workspace: $workspaceId and enqueued outbox entry ${outbox.id}",
        )
    }

    private fun serializeEvent(event: SubscriptionFormDeletedEvent): String =
        // Use a raw string to avoid escape characters which detekt flags.
        """{"formId":"${event.formId.value}","workspaceId":"${event.workspaceId.value}"}"""

    companion object {
        private val log = LoggerFactory.getLogger(FormDestroyer::class.java)
    }
}
