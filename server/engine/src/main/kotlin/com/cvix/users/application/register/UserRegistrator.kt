package com.cvix.users.application.register

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.event.EventBroadcaster
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.common.domain.vo.credential.Credential
import com.cvix.common.domain.vo.email.Email
import com.cvix.common.domain.vo.name.FirstName
import com.cvix.common.domain.vo.name.LastName
import com.cvix.users.domain.UserCreator
import com.cvix.users.domain.event.UserCreatedEvent
import java.util.UUID
import org.slf4j.LoggerFactory

/**
 * Service responsible for registering new users.
 *
 * @property userCreator The component responsible for creating user entities.
 * @property eventPublisher The publisher used to broadcast domain events.
 */
@Service
class UserRegistrator(
    private val userCreator: UserCreator,
    eventPublisher: EventPublisher<UserCreatedEvent>
) {
    private val eventBroadcaster = EventBroadcaster<UserCreatedEvent>()

    init {
        this.eventBroadcaster.use(eventPublisher)
    }

    /**
     * Registers a new user and broadcasts domain events.
     *
     * @param email The email address of the new user.
     * @param credential The credential (password) for the new user.
     * @param firstName The first name of the new user (nullable).
     * @param lastName The last name of the new user (nullable).
     * @return The UUID of the created user.
     *
     * Fails if user creation violates business rules or an unexpected error occurs.
     */
    suspend fun registerNewUser(
        email: Email,
        credential: Credential,
        firstName: FirstName?,
        lastName: LastName?
    ): UUID {
        log.debug("Registering new user with email: {}", email.value)

        val createdUser = userCreator.create(
            email = email,
            credential = credential,
            firstName = firstName,
            lastName = lastName,
        )
        val domainEvents = createdUser.pullDomainEvents()
        log.debug("Pulling {} events from created user", domainEvents.size)

        domainEvents.filterIsInstance<UserCreatedEvent>().forEach {
            eventBroadcaster.publish(it)
        }

        return createdUser.id.value
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserRegistrator::class.java)
    }
}
