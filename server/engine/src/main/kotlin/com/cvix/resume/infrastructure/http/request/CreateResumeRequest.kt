package com.cvix.resume.infrastructure.http.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.*

/**
 * Request DTO for creating a new resume.
 * Accepts JSON Resume schema content.
 */
data class CreateResumeRequest(
    @field:NotNull(message = "Workspace ID is required")
    val workspaceId: UUID,

    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String? = null,

    @field:NotNull(message = "Resume content is required")
    @field:Valid
    val content: GenerateResumeRequest,
)
