package com.loomify.users.infrastructure.event.consumer

import com.loomify.common.domain.bus.event.EventConsumer
import com.loomify.common.domain.bus.event.Subscribe
import com.loomify.users.domain.event.UserCreatedEvent
import com.loomify.users.infrastructure.persistence.keycloak.KeycloakRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Represents a class responsible for sending an email verification to a newly created user.
 *
 * This class is a component and an event consumer, subscribing to events of type UserCreatedEvent.
 * It uses a KeycloakRepository to perform the email verification process.
 *
 * @created 7/7/23
 * @param keycloakRepository the KeycloakRepository to use for performing the email verification process
 * @see KeycloakRepository for more information about the KeycloakRepository
 * @see Subscribe for more information about the Subscribe annotation
 * @see EventConsumer for more information about the EventConsumer interface
 * @see UserCreatedEvent for more information about the UserCreatedEvent
 */
@Component
@Subscribe(filterBy = UserCreatedEvent::class)
class SendEmailVerification(private val keycloakRepository: KeycloakRepository) :
    EventConsumer<UserCreatedEvent> {
    /**
     * Sends email verification to the user with the given user ID.
     *
     * @param event The UserCreatedEvent object representing the user creation event.
     */
    override suspend fun consume(event: UserCreatedEvent) {
        log.debug("Sending email verification to user with id: {}", event.id)
        runCatching {
            keycloakRepository.verify(event.id.toString())
        }.onFailure { ex ->
            log.warn("Failed to verify email for user id {}: {}", event.id, ex.message)
        }
    }
    companion object {
        private val log = LoggerFactory.getLogger(SendEmailVerification::class.java)
    }
}
