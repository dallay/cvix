package com.cvix.resume.infrastructure.http.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * Request DTO for creating a new resume.
 * Accepts JSON Resume schema content.
 *
 * Note: The workspace ID is obtained from the X-Workspace-Id header,
 * not from the request body. This ensures a single source of truth
 * for workspace context and enables Row-Level Security (RLS).
 */
data class CreateResumeRequest(
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String? = null,

    @field:NotNull(message = "Resume content is required")
    @field:Valid
    val content: GenerateResumeRequest,
)
