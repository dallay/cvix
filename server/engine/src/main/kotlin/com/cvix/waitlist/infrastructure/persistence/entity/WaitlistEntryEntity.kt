package com.cvix.waitlist.infrastructure.persistence.entity

import com.cvix.common.domain.AuditableEntityFields
import io.r2dbc.postgresql.codec.Json
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
 * R2DBC entity for waitlist entries.
 *
 * This entity represents a row in the `waitlist` table.
 *
 * Note: Implements AuditableEntityFields instead of extending AuditableEntity
 * to avoid Spring Data R2DBC 4.0 duplicate column mapping issue with inherited properties.
 */
@Table("waitlist")
data class WaitlistEntryEntity(
    @Id
    @JvmField
    val id: UUID,

    @Column("email")
    @get:Size(max = 320)
    val email: String,

    @Column("source_raw")
    @get:Size(max = 50)
    val sourceRaw: String,

    @Column("source_normalized")
    @get:Size(max = 50)
    val sourceNormalized: String,

    @Column("language")
    @get:Size(max = 10)
    val language: String,

    @Column("ip_hash")
    @get:Size(max = 64)
    val ipHash: String?,

    @Column("metadata")
    val metadata: Json?, // Stored as JSONB

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

    override fun getId(): UUID = id

    /**
     * Determines if the entity is new.
     * Uses isNewEntity() for waitlist entries which considers an entry new if it hasn't been updated.
     */
    override fun isNew(): Boolean = updatedAt == null

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
