package com.cvix.waitlist.domain

import com.cvix.common.domain.AggregateRoot
import com.cvix.common.domain.vo.email.Email
import com.cvix.waitlist.domain.event.WaitlistEntryCreatedEvent
import java.time.Instant

/**
 * Domain entity representing a waitlist entry.
 *
 * This aggregate root contains all information about a user who joined the waitlist
 * for early access to the application.
 *
 * @property id Unique identifier for the waitlist entry.
 * @property email The user's email address (validated).
 * @property sourceRaw The raw source string as provided by the client (for analytics and future growth).
 * @property sourceNormalized The normalized source enum value (for aggregated metrics and queries).
 * @property language The user's preferred language.
 * @property ipHash Anonymized hash of the user's IP address (for anti-spam).
 * @property metadata Additional JSON metadata (user agent, referrer, etc.).
 * @property createdAt Timestamp when the entry was created.
 * @property createdBy Who created the entry (default: "system").
 * @property updatedAt Timestamp of last update (not used for waitlist, but inherited from AggregateRoot).
 * @property updatedBy Who last updated the entry.
 */
data class WaitlistEntry(
    override val id: WaitlistEntryId,
    val email: Email,
    val sourceRaw: String,
    val sourceNormalized: WaitlistSource,
    val language: Language,
    val ipHash: String?,
    val metadata: Map<String, Any>? = null,
    override val createdAt: Instant = Instant.now(),
    override val createdBy: String = "system",
    override var updatedAt: Instant? = null,
    override var updatedBy: String? = null
) : AggregateRoot<WaitlistEntryId>() {

    init {
        // Validate IP hash if provided
        ipHash?.let {
            require(it.length == SHA256_HASH_LENGTH) { "IP hash must be a SHA-256 hash (64 hex characters)" }
            require(it.matches(Regex("^[a-fA-F0-9]{64}$"))) { "IP hash must be a valid SHA-256 hex string" }
        }
    }

    companion object {
        private const val SHA256_HASH_LENGTH = 64
        /**
         * Creates a new waitlist entry with the given information.
         *
         * @param id Unique identifier for the entry.
         * @param email User's email address.
         * @param sourceRaw The raw source string from the client.
         * @param sourceNormalized The normalized source enum value.
         * @param language User's preferred language.
         * @param ipHash Anonymized IP hash.
         * @param metadata Additional metadata.
         * @return A new WaitlistEntry aggregate root with a domain event recorded.
         */
        fun create(
            id: WaitlistEntryId,
            email: Email,
            sourceRaw: String,
            sourceNormalized: WaitlistSource,
            language: Language,
            ipHash: String? = null,
            metadata: Map<String, Any>? = null,
            createdAt: Instant = Instant.now(),
            createdBy: String = "system"
        ): WaitlistEntry {
            val entry = WaitlistEntry(
                id = id,
                email = email,
                sourceRaw = sourceRaw,
                sourceNormalized = sourceNormalized,
                language = language,
                ipHash = ipHash,
                metadata = metadata,
                createdAt = createdAt,
                createdBy = createdBy,
            )

            // Record domain event with both raw and normalized sources
            entry.record(
                WaitlistEntryCreatedEvent(
                    id = entry.id.id.toString(),
                    email = entry.email.value,
                    source = entry.sourceNormalized.value,
                    language = entry.language.code,
                ),
            )

            return entry
        }
    }
}
