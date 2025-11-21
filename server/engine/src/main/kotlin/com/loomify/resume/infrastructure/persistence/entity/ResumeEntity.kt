package com.loomify.resume.infrastructure.persistence.entity

import com.loomify.common.domain.AuditableEntity
import io.r2dbc.postgresql.codec.Json
import jakarta.validation.constraints.Size
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
    /**
     * This method returns the unique identifier of the workspace.
     *
     * @return The unique identifier of the workspace.
     */
    override fun getId(): UUID = id

    /**
     * This method checks if the workspace is new by comparing the creation and update timestamps.
     *
     * @return A boolean indicating whether the workspace is new.
     */
    override fun isNew(): Boolean = updatedAt == null || createdAt == updatedAt
}
