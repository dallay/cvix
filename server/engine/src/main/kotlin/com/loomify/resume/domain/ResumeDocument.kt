package com.loomify.resume.domain

import com.loomify.common.domain.AggregateRoot
import com.loomify.resume.domain.event.ResumeCreatedEvent
import com.loomify.resume.domain.event.ResumeUpdatedEvent
import java.time.Instant
import java.util.*

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
    override val id: ResumeDocumentId,
    val userId: UUID,
    val workspaceId: UUID,
    val title: String,
    val content: Resume,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val createdBy: String,
    override val updatedBy: String,
) : AggregateRoot<ResumeDocumentId>() {
    /**
     * Creates an updated copy of this document with new content.
     * Updates the title from the resume basics and sets the updatedAt timestamp.
     *
     * @param title New title for the resume document
     * @param newContent The new resume content
     * @param updatedBy User who is performing the update
     * @param now Timestamp of the update operation
     * @return A new ResumeDocument instance with updated data
     */
    fun update(
        title: String,
        newContent: Resume,
        updatedBy: String,
        now: Instant = Instant.now()
    ): ResumeDocument {
        val document = copy(
            title = title,
            content = newContent,
            updatedAt = now,
            updatedBy = updatedBy,
        )
        document.record(
            ResumeUpdatedEvent(
                resumeId = document.id.id,
                userId = document.userId,
                workspaceId = document.workspaceId,
            ),
        )
        return document
    }

    companion object {
        /**
         * Creates a new resume document from resume content.
         * Generates a new UUID and sets creation timestamps.
         *
         * @param id Unique identifier for the resume document
         * @param userId Owner of the resume
         * @param workspaceId Workspace this resume belongs to
         * @param title Display title (typically the person's name)
         * @param content The actual resume data following JSON Resume schema
         * @param createdBy User who is creating this document
         * @param now Timestamp of the creation operation
         * @return A new ResumeDocument instance
         */
        fun create(
            id: UUID,
            userId: UUID,
            workspaceId: UUID,
            title: String,
            content: Resume,
            createdBy: String,
            now: Instant = Instant.now(),
        ): ResumeDocument {
            val resumeDocument = ResumeDocument(
                id = ResumeDocumentId(id),
                userId = userId,
                workspaceId = workspaceId,
                title = title,
                content = content,
                createdAt = now,
                updatedAt = now,
                createdBy = createdBy,
                updatedBy = createdBy,
            )
            resumeDocument.record(
                ResumeCreatedEvent(
                    resumeDocument.id.id,
                    resumeDocument.userId,
                    resumeDocument.workspaceId,
                ),
            )
            return resumeDocument
        }
    }
}
