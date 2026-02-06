package com.cvix.resume.infrastructure.http.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * Request DTO for updating an existing resume.
 * Supports optimistic locking via expectedUpdatedAt.
 *
 * Note: The workspace ID is obtained from the X-Workspace-Id header,
 * not from the request body. This ensures a single source of truth
 * for workspace context and enables Row-Level Security (RLS).
 */
data class UpdateResumeRequest(
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String? = null,

    @field:NotNull(message = "Resume content is required")
    @field:Valid
    val content: ResumeContentRequest,

    /**
     * Expected last update timestamp for optimistic locking.
     * Format: ISO-8601 UTC instant (e.g., "2025-11-19T10:30:00Z").
     * Must be parsed to java.time.Instant at the boundary.
     */
    val expectedUpdatedAt: String? = null,
)
