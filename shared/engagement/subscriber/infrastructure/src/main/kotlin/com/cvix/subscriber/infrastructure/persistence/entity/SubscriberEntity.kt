package com.cvix.subscriber.infrastructure.persistence.entity

import com.cvix.common.domain.model.AuditableEntityFields
import com.cvix.subscriber.domain.Attributes
import com.cvix.subscriber.domain.SubscriberStatus
import jakarta.validation.constraints.Size
import java.io.Serializable
import java.time.Instant
import java.util.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * Entity representing a subscriber in the persistence layer.
 *
 * Maps to the `subscribers` table and includes audit fields.
 *
 * @property id Unique identifier for the subscriber.
 * @property email Subscriber's email address (max 320 chars).
 * @property source Source of the subscriber (max 50 chars).
 * @property sourceRaw Raw source value (max 50 chars).
 * @property status Current status of the subscriber.
 * @property language Preferred language (max 10 chars).
 * @property ipHash Optional hash of the subscriber's IP address (max 64 chars).
 * @property attributes Optional custom attributes for the subscriber.
 * @property confirmationToken Optional token for email confirmation.
 * @property confirmationExpiresAt Optional expiration time for confirmation token.
 * @property doNotContact Whether the subscriber should not be contacted.
 * @property workspaceId Associated workspace identifier.
 * @property createdBy User who created the entity (default: "system").
 * @property createdAt Timestamp when the entity was created.
 * @property updatedBy User who last updated the entity.
 * @property updatedAt Timestamp when the entity was last updated.
 *
 * @constructor Creates a new [SubscriberEntity].
 *
 * @created 19/1/26
 */
@Table("subscribers")
data class SubscriberEntity(
    @Id
    @JvmField
    val id: UUID,

    @field:Size(max = 320)
    @Column("email")
    val email: String,

    @Column("source")
    @get:Size(max = 50)
    val source: String,

    @Column("source_raw")
    @get:Size(max = 50)
    val sourceRaw: String,

    @Column("status")
    var status: SubscriberStatus,

    @Column("language")
    @get:Size(max = 10)
    val language: String,

    @Column("ip_hash")
    @get:Size(max = 64)
    val ipHash: String?,

    @Column("attributes")
    val attributes: Attributes? = Attributes(),

    @Column("confirmation_token")
    val confirmationToken: String? = null,

    @Column("confirmation_expires_at")
    val confirmationExpiresAt: Instant? = null,

    @Column("do_not_contact")
    val doNotContact: Boolean = false,

    @Column("workspace_id")
    val workspaceId: UUID,

    @CreatedBy
    @Column("created_by")
    @get:Size(max = 50)
    override val createdBy: String = "system",

    @CreatedDate
    @Column("created_at")
    override val createdAt: Instant,

    @LastModifiedBy
    @Column("updated_by")
    @get:Size(max = 50)
    override var updatedBy: String? = null,

    @LastModifiedDate
    @Column("updated_at")
    override var updatedAt: Instant? = null,
) : Serializable, Persistable<UUID>, AuditableEntityFields {

    /**
     * Returns the unique identifier of the subscriber.
     */
    override fun getId(): UUID = id

    /**
     * Determines if the entity is new.
     * Delegates to the interface's default implementation which checks if the entity
     * has not been updated (`updatedAt == null`) or was just created (`createdAt == updatedAt`).
     *
     * @return `true` if the entity is new, `false` otherwise.
     */
    override fun isNew(): Boolean = isNewEntity()

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
