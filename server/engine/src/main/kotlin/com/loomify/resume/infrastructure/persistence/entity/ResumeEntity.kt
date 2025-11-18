package com.loomify.resume.infrastructure.persistence.entity

import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * Database entity for Resume persistence.
 * Maps to the 'resumes' table in PostgreSQL.
 * Stores resume data as JSONB text.
 */
@Table("resumes")
data class ResumeEntityDb(
    @Id
    val id: UUID,

    @Column("user_id")
    val userId: UUID,

    @Column("workspace_id")
    val workspaceId: UUID,

    @Column("title")
    val title: String,

    @Column("data")
    val data: String, // JSON string

    @Column("created_by")
    val createdBy: String,

    @Column("created_at")
    val createdAt: Instant,

    @Column("updated_by")
    val updatedBy: String? = null,

    @Column("updated_at")
    val updatedAt: Instant? = null,
)

