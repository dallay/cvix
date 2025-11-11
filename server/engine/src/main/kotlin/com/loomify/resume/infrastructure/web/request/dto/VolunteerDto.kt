package com.loomify.resume.infrastructure.web.request.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for volunteer experience.
 */
data class VolunteerDto(
    @field:NotBlank(message = "Organization is required")
    @field:Size(max = 100, message = "Organization must not exceed 100 characters")
    val organization: String,

    @field:NotBlank(message = "Position is required")
    @field:Size(max = 100, message = "Position must not exceed 100 characters")
    val position: String,

    val url: String? = null,

    val startDate: String? = null,

    val endDate: String? = null,

    @field:Size(max = 500, message = "Summary must not exceed 500 characters")
    val summary: String? = null,

    val highlights: List<
        @Size(max = 500, message = "Highlight must not exceed 500 characters")
        String,
        >? = null
)
