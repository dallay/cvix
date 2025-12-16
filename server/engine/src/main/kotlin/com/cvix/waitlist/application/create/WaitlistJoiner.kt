package com.cvix.waitlist.application.create

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.event.EventBroadcaster
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.common.domain.vo.email.Email
import com.cvix.waitlist.domain.EmailAlreadyExistsException
import com.cvix.waitlist.domain.Language
import com.cvix.waitlist.domain.WaitlistEntry
import com.cvix.waitlist.domain.WaitlistEntryId
import com.cvix.waitlist.domain.WaitlistRepository
import com.cvix.waitlist.domain.WaitlistSource
import com.cvix.waitlist.domain.event.WaitlistEntryCreatedEvent
import java.security.MessageDigest
import org.slf4j.LoggerFactory

/**
 * Service responsible for adding users to the waitlist.
 *
 * This service validates user input, checks for duplicates, and persists
 * new waitlist entries to the repository.
 *
 * @property repository The repository for managing waitlist entries.
 */
@Service
class WaitlistJoiner(
    private val repository: WaitlistRepository,
    eventPublisher: EventPublisher<WaitlistEntryCreatedEvent>
) {
    private val eventBroadcaster = EventBroadcaster<WaitlistEntryCreatedEvent>()

    init {
        this.eventBroadcaster.use(eventPublisher)
    }

    /**
     * Adds a new user to the waitlist.
     *
     * @param id Unique identifier for the entry.
     * @param email User's email address (will be validated).
     * @param source Source from where user is joining.
     * @param language User's preferred language.
     * @param ipAddress User's IP address (will be hashed).
     * @param metadata Additional metadata.
     * @return The created waitlist entry.
     * @throws EmailAlreadyExistsException if the email already exists in the waitlist.
     */
    suspend fun join(
        id: WaitlistEntryId,
        email: Email,
        source: WaitlistSource,
        language: Language,
        ipAddress: String? = null,
        metadata: Map<String, Any>? = null
    ): WaitlistEntry {
        logger.info("Attempting to add email to waitlist from source: {}", source.value)

        // Check if email already exists
        val exists = repository.existsByEmail(email)
        if (exists) {
            logger.warn("Email already exists in waitlist")
            throw EmailAlreadyExistsException(email.value)
        }

        // Hash IP address for privacy
        val ipHash = ipAddress?.let { hashIpAddress(it) }

        // Create the waitlist entry
        val entry = WaitlistEntry.create(
            id = id,
            email = email,
            source = source,
            language = language,
            ipHash = ipHash,
            metadata = metadata,
        )

        // Save to repository
        val savedEntry = repository.save(entry)
        logger.info("Successfully added email to waitlist with ID: {}", entry.id.id)
        // Publish domain event
        savedEntry.pullDomainEvents().forEach { event ->
            eventBroadcaster.publish(event as WaitlistEntryCreatedEvent)
        }
        return savedEntry
    }

    /**
     * Hashes an IP address using SHA-256 for anonymization.
     *
     * @param ipAddress The IP address to hash.
     * @return The SHA-256 hash as a hexadecimal string.
     */
    private fun hashIpAddress(ipAddress: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(ipAddress.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WaitlistJoiner::class.java)
    }
}
