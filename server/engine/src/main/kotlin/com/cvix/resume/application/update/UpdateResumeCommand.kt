package com.cvix.resume.application.update

import com.cvix.common.domain.bus.command.Command
import com.cvix.resume.domain.Resume
import java.util.UUID

/**
 * Command to update an existing resume document.
 * Part of the CQRS pattern in the application layer.
 *
 * Implements optimistic locking by checking updatedAt timestamp.
 *
 * @property id The resume document ID to update
 * @property userId The authenticated user (must be owner)
 * @property workspaceId The workspace ID where the resume belongs
 * @property title The new title of the resume (optional)
 * @property content The new resume data
 * @property updatedBy Username or email of the updater
 * @property expectedUpdatedAt Expected last update timestamp (for optimistic locking)
 */
data class UpdateResumeCommand(
    val id: UUID,
    val userId: UUID,
    val workspaceId: UUID,
    val title: String?,
    val content: Resume,
    val updatedBy: String,
    /**
     * Expected last update timestamp (for optimistic locking).
     * ISO-8601 UTC format (e.g., 2025-11-22T12:34:56Z).
     */
    val expectedUpdatedAt: java.time.Instant? = null,
) : Command
