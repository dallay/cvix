package com.cvix.resume.infrastructure.persistence.entity

import com.cvix.common.domain.SYSTEM_USER
import com.cvix.common.domain.model.AuditableEntityFields
import io.r2dbc.postgresql.codec.Json
import jakarta.validation.constraints.Size
import java.io.Serializable
import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * Database entity for Resume persistence.
 * Maps to the 'resumes' table in PostgreSQL.
 * Stores resume data as JSONB text.
 *
 * Note: Implements AuditableEntityFields instead of extending AuditableEntity
 * to avoid Spring Data R2DBC 4.0 duplicate column mapping issue with inherited properties.
 */
@Table("resumes")
data class ResumeEntity(
    @Id
    @JvmField
    val id: UUID,

    @Column("user_id")
    val userId: UUID,

    @Column("workspace_id")
    val workspaceId: UUID,

    @Column("title")
    val title: String,

    @Column("data")
    val data: Json, // JSONB column

    @CreatedBy
    @Column("created_by")
    @get:Size(max = 50)
    override val createdBy: String = SYSTEM_USER,

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
     * This method returns the unique identifier of the resume.
     *
     * @return The unique identifier of the resume.
     */
    override fun getId(): UUID = id

    /**
     * This method checks if the resume is new by comparing the creation and update timestamps.
     * Delegates to the interface's default implementation.
     *
     * @return A boolean indicating whether the resume is new.
     */
    override fun isNew(): Boolean = isNewEntity()

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
