package com.cvix.waitlist.infrastructure.persistence.entity

import com.cvix.common.domain.AuditableEntity
import io.r2dbc.postgresql.codec.Json
import jakarta.validation.constraints.Size
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
 */
@Table("waitlist")
data class WaitlistEntryEntity(
    @Id
    @JvmField
    val id: UUID,

    @Column("email")
    @get:Size(max = 320)
    val email: String,

    @Column("source")
    @get:Size(max = 50)
    val source: String,

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
    override var updatedAt: Instant? = null
) : AuditableEntity(createdAt, createdBy, updatedAt, updatedBy), Persistable<UUID> {

    override fun getId(): UUID = id

    override fun isNew(): Boolean = updatedAt == null || createdAt == updatedAt
}
