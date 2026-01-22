package com.cvix.waitlist.application.create

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.event.EventBroadcaster
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.common.domain.model.Language
import com.cvix.common.domain.security.Hasher
import com.cvix.common.domain.vo.email.Email
import com.cvix.waitlist.domain.EmailAlreadyExistsException
import com.cvix.waitlist.domain.WaitlistEntry
import com.cvix.waitlist.domain.WaitlistEntryId
import com.cvix.waitlist.domain.WaitlistMetrics
import com.cvix.waitlist.domain.WaitlistRepository
import com.cvix.waitlist.domain.WaitlistSecurityConfig
import com.cvix.waitlist.domain.WaitlistSource
import com.cvix.waitlist.domain.event.WaitlistEntryCreatedEvent
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
    eventPublisher: EventPublisher<WaitlistEntryCreatedEvent>,
    private val metrics: WaitlistMetrics,
    private val securityConfig: WaitlistSecurityConfig,
    private val hasher: Hasher,
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
     * @param sourceRaw The raw source string from the client.
     * @param sourceNormalized The normalized source enum value.
     * @param language User's preferred language.
     * @param ipAddress User's IP address (will be hashed).
     * @param metadata Additional metadata.
     * @return The created waitlist entry.
     * @throws EmailAlreadyExistsException if the email already exists in the waitlist.
     */
    suspend fun join(
        id: WaitlistEntryId,
        email: Email,
        sourceRaw: String,
        sourceNormalized: WaitlistSource,
        language: Language,
        ipAddress: String? = null,
        metadata: Map<String, Any>? = null
    ): WaitlistEntry {
        logger.info(
            "Attempting to add email to waitlist from source: raw='{}', normalized='{}'",
            sourceRaw,
            sourceNormalized.value,
        )

        // Record metrics for source tracking
        metrics.recordWaitlistJoin(sourceRaw, sourceNormalized)
        metrics.recordSourceNormalization(sourceRaw, sourceNormalized)

        // Check if email already exists
        val exists = repository.existsByEmail(email)
        if (exists) {
            logger.warn("Email already exists in waitlist")
            throw EmailAlreadyExistsException(email.value)
        }

        // Hash IP address for privacy
        val ipHash = ipAddress?.let { hashIpAddress(it) }

        // Create the waitlist entry with both raw and normalized sources
        val entry = WaitlistEntry.create(
            id = id,
            email = email,
            sourceRaw = sourceRaw,
            sourceNormalized = sourceNormalized,
            language = language,
            ipHash = ipHash,
            metadata = metadata,
        )

        // Save to repository
        val savedEntry = repository.save(entry)
        logger.info(
            "Successfully added email to waitlist with ID: {}, sourceRaw: {}, sourceNormalized: {}",
            entry.id.id,
            sourceRaw,
            sourceNormalized.value,
        )
        // Publish domain event
        savedEntry.pullDomainEvents().forEach { event ->
            eventBroadcaster.publish(event as WaitlistEntryCreatedEvent)
        }
        return savedEntry
    }

    /**
     * Hashes an IP address using HMAC-SHA256 for anonymization.
     *
     * @param ipAddress The IP address to hash.
     * @return The HMAC-SHA256 hash as a hexadecimal string.
     */
    private fun hashIpAddress(ipAddress: String): String {
        // Security config still validates that secret exists in infra when HMAC is required.
        require(securityConfig.ipHmacSecret.isNotBlank()) {
            "No HMAC secret configured for IP address hashing. Define waitlist.security.ip-hmac-secret"
        }
        return hasher.hash(ipAddress)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WaitlistJoiner::class.java)
    }
}
