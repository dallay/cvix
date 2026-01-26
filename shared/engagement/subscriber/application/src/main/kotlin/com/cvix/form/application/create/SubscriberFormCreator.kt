package com.cvix.form.application.create

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.event.EventBroadcaster
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.form.domain.SubscriptionForm
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormRepository
import com.cvix.form.domain.SubscriptionFormSettings
import com.cvix.form.domain.event.SubscriptionFormCreatedEvent
import java.util.*
import org.slf4j.LoggerFactory

/**
 * Application service for creating subscription forms.
 *
 * This service handles the business logic for creating a new `SubscriptionForm`. It orchestrates
 * the instantiation of the form, persistence via the repository, and the publication of
 * a domain event to notify other parts of the system.
 *
 * @property formRepository The repository for data access to subscription forms.
 * @param eventPublisher A publisher to which form creation events will be dispatched.
 */
@Service
class SubscriberFormCreator(
    private val formRepository: SubscriptionFormRepository,
    eventPublisher: EventPublisher<SubscriptionFormCreatedEvent>,
) {
    private val broadcaster = EventBroadcaster<SubscriptionFormCreatedEvent>()

    init {
        this.broadcaster.use(eventPublisher)
    }

    /**
     * Creates a new subscription form, persists it, and publishes a creation event.
     *
     * @param formId The unique identifier for the new form.
     * @param name The name of the form.
     * @param description A brief description of the form's purpose.
     * @param settings Configuration details for the form's behavior and appearance.
     * @param workspaceId The ID of the workspace the form belongs to.
     * @return The newly created [SubscriptionForm] instance.
     */
    suspend fun create(
        formId: UUID,
        name: String,
        description: String,
        settings: SubscriptionFormSettings,
        workspaceId: UUID,
    ): SubscriptionForm {
        log.debug("Creating form with name: {}", name)

        // Create the domain entity using its factory method
        val form = SubscriptionForm.create(
            id = SubscriptionFormId(formId),
            name = name,
            description = description,
            settings = settings,
            workspaceId = WorkspaceId(workspaceId),
        )

        // Persist the new form
        formRepository.create(form)

        // Publish domain events
        val domainEvents = form.pullDomainEvents()
        log.debug("Pulling {} event(s) from form {}", domainEvents.size, form.id)
        domainEvents.filterIsInstance<SubscriptionFormCreatedEvent>().forEach { event ->
            broadcaster.publish(event)
        }

        return form
    }

    companion object {
        private val log = LoggerFactory.getLogger(SubscriberFormCreator::class.java)
    }
}
