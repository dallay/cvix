package com.cvix.form.application.update

import com.cvix.common.domain.Service
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.outbox.OutboxEntry
import com.cvix.common.domain.outbox.OutboxRepository
import com.cvix.form.domain.SubscriptionFormFinderRepository
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormRepository
import com.cvix.form.domain.SubscriptionFormSettings
import com.cvix.form.domain.event.SubscriptionFormUpdatedEvent
import com.cvix.form.domain.exception.SubscriptionFormNotFoundException
import java.time.Instant
import java.util.*
import org.slf4j.LoggerFactory
import tools.jackson.databind.json.JsonMapper

/**
 * Application service for updating subscription forms.
 *
 * This service updates subscription forms and persists domain events to an outbox
 * for reliable eventual delivery. Events are stored in the same transaction as
 * the form update to ensure consistency.
 */
@Service
class SubscriberFormUpdater(
    private val formRepository: SubscriptionFormRepository,
    private val formFinder: SubscriptionFormFinderRepository,
    private val outboxRepository: OutboxRepository,
    private val jsonMapper: JsonMapper,
) {

    /**
     * Updates an existing subscription form.
     *
     * The update events are persisted to the outbox within the same transaction
     * as the form update. A separate worker reads the outbox and publishes events
     * to ensure reliable delivery even if the publish call fails after the
     * transaction commits.
     */
    suspend fun update(
        workspaceId: WorkspaceId,
        formId: SubscriptionFormId,
        name: String,
        description: String,
        settings: SubscriptionFormSettings,
        updatedBy: String,
    ) {
        log.debug("Updating form with ID: {} in workspace: {}", formId, workspaceId)

        val form = formFinder.findByFormIdAndWorkspaceId(
            formId = formId,
            workspaceId = workspaceId,
        )
            ?: throw SubscriptionFormNotFoundException(
                "Subscription form with ID $formId not found in workspace $workspaceId",
            )

        if (form.name == name && form.description == description && form.settings == settings) {
            log.debug("No changes detected for form with ID: {}", formId)
            return
        }

        val updatedForm = form.updateDetails(
            name = name,
            description = description,
            settings = settings,
            updatedBy = updatedBy,
        )

        formRepository.update(updatedForm)

        // Persist events to outbox for reliable delivery
        val domainEvents = updatedForm.pullDomainEvents()
        domainEvents.filterIsInstance<SubscriptionFormUpdatedEvent>().forEach { event ->
            val outbox = OutboxEntry(
                id = UUID.randomUUID(),
                aggregateType = "SubscriptionForm",
                aggregateId = event.formId.value.toString(),
                eventType = event::class.simpleName ?: "SubscriptionFormUpdatedEvent",
                payload = serializeEvent(event),
                occurredAt = Instant.now(),
            )
            outboxRepository.save(outbox)
            log.debug("Enqueued outbox entry {} for form update event", outbox.id)
        }
    }

    private fun serializeEvent(event: SubscriptionFormUpdatedEvent): String {
        val payload = mapOf(
            "formId" to event.formId.value.toString(),
            "workspaceId" to event.workspaceId.value.toString(),
            "updatedAt" to event.updatedAt.toString(),
            "updatedBy" to event.updatedBy,
        )
        return jsonMapper.writeValueAsString(payload)
    }

    companion object {
        private val log = LoggerFactory.getLogger(SubscriberFormUpdater::class.java)
    }
}
