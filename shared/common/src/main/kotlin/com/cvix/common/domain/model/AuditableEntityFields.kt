package com.cvix.common.domain.model

import java.time.Instant

/**
 * Interface defining auditable fields for entities.
 * Provides a reusable contract for entities that need audit tracking
 * without requiring class inheritance (useful for R2DBC entities with Spring Data 4.0).
 *
 * Implementers should annotate properties with appropriate Spring Data annotations:
 * - @CreatedBy, @CreatedDate, @LastModifiedBy, @LastModifiedDate
 *
 * Example:
 * ```kotlin
 * @Table("my_table")
 * data class MyEntity(
 *     @Id val id: UUID,
 *     @CreatedBy @Column("created_by") override val createdBy: String = "system",
 *     @CreatedDate @Column("created_at") override val createdAt: Instant,
 *     @LastModifiedBy @Column("updated_by") override var updatedBy: String? = null,
 *     @LastModifiedDate @Column("updated_at") override var updatedAt: Instant? = null,
 * ) : AuditableEntityFields, Persistable<UUID> {
 *     override fun getId(): UUID = id
 * }
 * ```
 */
interface AuditableEntityFields {
    /**
     * The user or system that created the entity.
     */
    val createdBy: String

    /**
     * The timestamp when the entity was created.
     */
    val createdAt: Instant

    /**
     * The user or system that last updated the entity.
     */
    var updatedBy: String?

    /**
     * The timestamp when the entity was last updated.
     */
    var updatedAt: Instant?

    /**
     * Determines if the entity is new by comparing timestamps.
     * An entity is considered new if it has not been updated yet.
     *
     * @return true if updatedAt is null or equals createdAt, false otherwise
     */
    fun isNewEntity(): Boolean = updatedAt == null || createdAt == updatedAt
}
