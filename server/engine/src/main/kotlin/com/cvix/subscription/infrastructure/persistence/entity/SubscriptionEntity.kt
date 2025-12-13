package com.cvix.subscription.infrastructure.persistence.entity

import java.time.Instant
import java.util.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * R2DBC entity representing a row in the subscriptions table.
 *
 * @created 12/11/25
 */
@Table("subscriptions")
data class SubscriptionEntity(
    @Id
    @JvmField
    val id: UUID,

    @Column("user_id")
    val userId: UUID,

    val tier: String,

    val status: String,

    @Column("valid_from")
    val validFrom: Instant,

    @Column("valid_until")
    val validUntil: Instant? = null,

    @CreatedDate
    @Column("created_at")
    val createdAt: Instant? = null,

    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: Instant? = null
) : Persistable<UUID> {
    override fun getId(): UUID = id

    // Consider new when createdAt is null so Spring Data issues INSERT
    override fun isNew(): Boolean = createdAt == null
}
