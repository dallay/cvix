package com.loomify.resume.infrastructure.web.request.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for work experience following JSON Resume schema.
 * Maps to items in the 'work' array in JSON Resume schema.
 */
data class WorkExperienceDto(
    @field:NotBlank(message = "Company name is required")
    @field:Size(max = 100, message = "Company name must not exceed 100 characters")
    val name: String,

    @field:NotBlank(message = "Position is required")
    @field:Size(max = 100, message = "Position must not exceed 100 characters")
    val position: String,

    @field:NotBlank(message = "Start date is required")
    val startDate: String, // ISO date format: YYYY-MM-DD or YYYY-MM or YYYY

    val endDate: String? = null, // ISO date format: YYYY-MM-DD or YYYY-MM or YYYY

    @field:Size(max = 200, message = "Location must not exceed 200 characters")
    val location: String? = null,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String? = null,

    @field:Size(max = 500, message = "Summary must not exceed 500 characters")
    val summary: String? = null,

    val url: String? = null,

    val highlights: List<
        @Size(max = 500, message = "Highlight must not exceed 500 characters")
        String,
        >? = null
)
