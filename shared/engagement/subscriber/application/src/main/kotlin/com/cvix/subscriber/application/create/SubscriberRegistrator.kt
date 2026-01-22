package com.cvix.subscriber.application.create

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.event.EventBroadcaster
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.common.domain.model.Language
import com.cvix.common.domain.security.Hasher
import com.cvix.subscriber.domain.Attributes
import com.cvix.subscriber.domain.Subscriber
import com.cvix.subscriber.domain.SubscriberRepository
import com.cvix.subscriber.domain.event.SubscriberCreatedEvent
import java.util.*

/**
 * Service class responsible for registering subscribers.
 *
 * @property subscriberRepository The repository for managing subscribers.
 * @property eventPublisher The publisher for broadcasting domain events.
 * @property hasher The hasher for anonymizing sensitive data.
 */
@Service
class SubscriberRegistrator(
    private val subscriberRepository: SubscriberRepository,
    eventPublisher: EventPublisher<SubscriberCreatedEvent>,
    private val hasher: Hasher
) {
    private val eventPublisher = EventBroadcaster<SubscriberCreatedEvent>()

    init {
        this.eventPublisher.use(eventPublisher)
    }

    /**
     * Registers a new subscriber and publishes a creation event.
     *
     * @param id Unique identifier for the subscriber.
     * @param email Email address of the subscriber.
     * @param source Source of the subscription.
     * @param language Preferred language of the subscriber.
     * @param ipAddress Optional IP address of the subscriber.
     * @param attributes Optional additional attributes for the subscriber.
     * @param workspaceI Identifier of the workspace associated with the subscriber.
     */
    suspend fun register(
        id: UUID,
        email: String,
        source: String,
        language: Language,
        ipAddress: String? = null,
        attributes: Attributes?,
        workspaceI: UUID
    ) {

        val subscriber = Subscriber.create(
            id = id,
            email = email,
            source = source,
            language = language,
            ipAddress = ipAddress,
            hasher = hasher,
            attributes = attributes,
            workspaceId = workspaceI,
        )
        subscriberRepository.create(subscriber)
        val domainEvents = subscriber.pullDomainEvents()

        domainEvents.filterIsInstance<SubscriberCreatedEvent>().forEach {
            eventPublisher.publish(it)
        }
    }
}
