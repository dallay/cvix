package com.cvix.form.application.update

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.event.EventBroadcaster
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.form.domain.SubscriptionFormFinderRepository
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormRepository
import com.cvix.form.domain.SubscriptionFormSettings
import com.cvix.form.domain.event.SubscriptionFormUpdatedEvent
import com.cvix.form.domain.exception.SubscriptionFormNotFoundException
import java.util.*
import org.slf4j.LoggerFactory

/**
 * Application service for updating subscription forms.
 */
@Service
class SubscriberFormUpdater(
    private val formRepository: SubscriptionFormRepository,
    private val formFinder: SubscriptionFormFinderRepository,
    eventPublisher: EventPublisher<SubscriptionFormUpdatedEvent>,
) {
    private val broadcaster = EventBroadcaster<SubscriptionFormUpdatedEvent>()

    init {
        this.broadcaster.use(eventPublisher)
    }

    /**
     * Updates an existing subscription form.
     */
    suspend fun update(
        workspaceId: UUID,
        formId: UUID,
        name: String,
        description: String,
        settings: SubscriptionFormSettings,
        updatedBy: String
    ) {
        log.debug("Updating form with ID: {} in workspace: {}", formId, workspaceId)

        val form = formFinder.findByFormIdAndWorkspaceId(
            formId = SubscriptionFormId(formId),
            workspaceId = WorkspaceId(workspaceId),
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

        val domainEvents = updatedForm.pullDomainEvents()
        domainEvents.filterIsInstance<SubscriptionFormUpdatedEvent>().forEach { event ->
            broadcaster.publish(event)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SubscriberFormUpdater::class.java)
    }
}
