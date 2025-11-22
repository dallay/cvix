package com.loomify.resume.infrastructure.http.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

/**
 * Request DTO for updating an existing resume.
 * Supports optimistic locking via expectedUpdatedAt.
 */
data class UpdateResumeRequest(
    @field:NotNull(message = "Workspace ID is required")
    val workspaceId: UUID,

    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String? = null,

    @field:NotNull(message = "Resume content is required")
    @field:Valid
    val content: GenerateResumeRequest,

    /**
     * Expected last update timestamp for optimistic locking.
     * Format: ISO-8601 UTC instant (e.g., "2025-11-19T10:30:00Z").
     * Must be parsed to java.time.Instant at the boundary.
     */
    val expectedUpdatedAt: String? = null,
)
