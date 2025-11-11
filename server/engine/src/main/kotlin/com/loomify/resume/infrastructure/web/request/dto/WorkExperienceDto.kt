package com.loomify.resume.infrastructure.web.request.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for work experience.
 */
data class WorkExperienceDto(
    @field:NotBlank(message = "Company is required")
    @field:Size(max = 100, message = "Company must not exceed 100 characters")
    val company: String,

    @field:NotBlank(message = "Position is required")
    @field:Size(max = 100, message = "Position must not exceed 100 characters")
    val position: String,

    @field:NotBlank(message = "Start date is required")
    val startDate: String, // ISO date format: YYYY-MM-DD

    val endDate: String? = null, // ISO date format: YYYY-MM-DD

    @field:Size(max = 200, message = "Location must not exceed 200 characters")
    val location: String? = null,

    @field:Size(max = 500, message = "Summary must not exceed 500 characters")
    val summary: String? = null,

    // Deprecated: Use summary instead. Kept for backward compatibility.
    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String? = null,

    val url: String? = null,

    val highlights: List<
        @Size(max = 500, message = "Highlight must not exceed 500 characters")
        String,
        >? = null
)
