package com.loomify.resume.domain

import java.time.Instant
import java.util.UUID

/**
 * Resume document aggregate root.
 *
 * Combines the pure Resume content (JSON Resume schema) with persistence metadata.
 * This represents a resume as a first-class entity in the domain with identity and lifecycle.
 *
 * Following DDD principles:
 * - Resume: Value object containing the actual CV data (pure, immutable)
 * - ResumeDocument: Entity/Aggregate root with identity and metadata
 *
 * @property id Unique identifier for this resume document
 * @property userId Owner of the resume
 * @property workspaceId Workspace this resume belongs to
 * @property title Display title (typically the person's name)
 * @property content The actual resume data following JSON Resume schema
 * @property createdAt When this document was created
 * @property updatedAt When this document was last modified
 * @property createdBy User who created this document
 * @property updatedBy User who last modified this document
 */
data class ResumeDocument(
    val id: UUID,
    val userId: UUID,
    val workspaceId: UUID,
    val title: String,
    val content: Resume,
    val createdAt: Instant,
    val updatedAt: Instant,
    val createdBy: String,
    val updatedBy: String,
) {
    /**
     * Creates an updated copy of this document with new content.
     * Updates the title from the resume basics and sets the updatedAt timestamp.
     */
    fun update(newContent: Resume, updatedBy: String, now: Instant = Instant.now()): ResumeDocument {
        return copy(
            title = newContent.basics.name.value,
            content = newContent,
            updatedAt = now,
            updatedBy = updatedBy,
        )
    }

    companion object {
        /**
         * Creates a new resume document from resume content.
         * Generates a new UUID and sets creation timestamps.
         */
        fun create(
            userId: UUID,
            workspaceId: UUID,
            content: Resume,
            createdBy: String,
            now: Instant = Instant.now(),
        ): ResumeDocument {
            return ResumeDocument(
                id = UUID.randomUUID(),
                userId = userId,
                workspaceId = workspaceId,
                title = content.basics.name.value,
                content = content,
                createdAt = now,
                updatedAt = now,
                createdBy = createdBy,
                updatedBy = createdBy,
            )
        }
    }
}
