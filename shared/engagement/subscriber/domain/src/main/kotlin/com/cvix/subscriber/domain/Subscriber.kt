package com.cvix.subscriber.domain

import com.cvix.common.domain.SYSTEM_USER
import com.cvix.common.domain.model.AggregateRoot
import com.cvix.common.domain.model.Language
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.security.Hasher
import com.cvix.common.domain.vo.email.Email
import com.cvix.common.domain.vo.ip.IpHash
import com.cvix.subscriber.domain.event.SubscriberCreatedEvent
import java.time.Instant
import java.util.UUID

/**
 * Represents a subscriber in the newsletter domain.
 *
 * @property id The unique identifier of the subscriber.
 * @property email The email address of the subscriber.
 * @property source The source of the subscription (e.g., web, API).
 * @property sourceRaw The raw source string as received.
 * @property status The current status of the subscriber (e.g., PENDING, ACTIVE).
 * @property attributes Additional attributes associated with the subscriber.
 * @property language The preferred language of the subscriber.
 * @property ipHash The hash of the subscriber's IP address, if provided.
 *          See [com.cvix.common.domain.security.Hasher]
 * @property confirmationToken The token used for confirming the subscription.
 * @property confirmationExpiresAt The expiration time of the confirmation token.
 * @property doNotContact Indicates if the subscriber should not be contacted.
 * @property workspaceId The identifier of the workspace the subscriber belongs to.
 * @property createdAt The timestamp when the subscriber was created.
 * @property createdBy The identifier of the creator (default: "system").
 * @property updatedAt The timestamp when the subscriber was last updated.
 * @property updatedBy The identifier of the last updater.
 */
data class Subscriber(
    override val id: SubscriberId,
    val email: Email,
    val source: SubscriptionSource,
    val sourceRaw: String,
    var status: SubscriberStatus = SubscriberStatus.PENDING,
    val attributes: Attributes? = Attributes(),
    val language: Language,
    val ipHash: IpHash? = null,
    val confirmationToken: String? = null,
    val confirmationExpiresAt: Instant? = null,
    val doNotContact: Boolean = false,
    val workspaceId: WorkspaceId,
    override val createdAt: Instant = Instant.now(),
    override val createdBy: String = SYSTEM_USER,
    override var updatedAt: Instant? = null,
    override var updatedBy: String? = null
) : AggregateRoot<SubscriberId>() {

    /**
     * Updates the status of the subscriber.
     *
     * @param status The new status of the subscriber.
     */
    fun updateStatus(status: SubscriberStatus) {
        this.status = status
    }

    companion object {

        /**
         * Factory method to create a new [Subscriber] instance and record a [SubscriberCreatedEvent].
         *
         * @param id The unique identifier of the subscriber.
         * @param email The email address of the subscriber.
         * @param source The source of the subscription.
         * @param language The preferred language of the subscriber. Defaults to [Language.ENGLISH].
         * @param ipAddress The raw IP address of the subscriber to be hashed.
         * @param hasher The hasher to use for anonymizing the IP address.
         * @param attributes Additional attributes associated with the subscriber.
         * @param workspaceId The workspace identifier.
         *
         * @return A [Subscriber]
         */
        fun create(
            id: UUID,
            email: String,
            source: String,
            language: Language = Language.ENGLISH,
            ipAddress: String? = null,
            hasher: Hasher? = null,
            attributes: Attributes? = null,
            workspaceId: UUID,
        ): Subscriber {
            val hashedIp = ipAddress?.let {
                requireNotNull(hasher) { "Hasher is required when ipAddress is provided" }
                IpHash.from(hasher.hash(it))
            }

            val subscriber = Subscriber(
                id = SubscriberId(id),
                email = Email(email),
                source = SubscriptionSource(source),
                sourceRaw = source,
                language = language,
                ipHash = hashedIp,
                attributes = attributes,
                workspaceId = WorkspaceId(workspaceId),
            )
            subscriber.record(
                SubscriberCreatedEvent(
                    id = UUID.randomUUID().toString(),
                    aggregateId = subscriber.id.value.toString(),
                    email = subscriber.email.value,
                    status = subscriber.status.toString(),
                    attributes = subscriber.attributes,
                    source = subscriber.source.source,
                    language = subscriber.language,
                    ipAddress = hashedIp?.value,
                    workspaceId = subscriber.workspaceId.value.toString(),
                    createdAt = subscriber.createdAt,
                ),
            )
            return subscriber
        }
    }
}
