package com.cvix.form.application.delete

import com.cvix.common.domain.SYSTEM_USER_UUID
import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.event.EventBroadcaster
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.outbox.OutboxEntry
import com.cvix.common.domain.outbox.OutboxRepository
import com.cvix.form.domain.SubscriptionFormFinderRepository
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormRepository
import com.cvix.form.domain.event.ActorId
import com.cvix.form.domain.event.SubscriptionFormDeletedEvent
import java.time.Instant
import java.util.*
import org.slf4j.LoggerFactory
import tools.jackson.databind.json.JsonMapper

@Service
class FormDestroyer(
    private val formRepository: SubscriptionFormRepository,
    private val formFinder: SubscriptionFormFinderRepository,
    eventPublisher: EventPublisher<SubscriptionFormDeletedEvent>,
    private val outboxRepository: OutboxRepository,
    private val jsonMapper: JsonMapper,
) {
    private val broadcaster = EventBroadcaster<SubscriptionFormDeletedEvent>()

    init {
        this.broadcaster.use(eventPublisher)
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
            deletedBy = ActorId(SYSTEM_USER_UUID),
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

    private fun serializeEvent(event: SubscriptionFormDeletedEvent): String {
        val payload = mapOf(
            "formId" to event.formId.value.toString(),
            "workspaceId" to event.workspaceId.value.toString(),
        )
        return jsonMapper.writeValueAsString(payload)
    }

    companion object {
        private val log = LoggerFactory.getLogger(FormDestroyer::class.java)
    }
}
