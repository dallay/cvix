package com.loomify.resume.infrastructure.web.request.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * DTO for project.
 */
data class ProjectDto(
    @field:NotBlank(message = "Project name is required")
    @field:Size(max = 100, message = "Project name must not exceed 100 characters")
    val name: String,

    @field:NotBlank(message = "Project description is required")
    @field:Size(max = 500, message = "Project description must not exceed 500 characters")
    val description: String,

    val url: String? = null,

    @field:Pattern(
        regexp = """^\d{4}-\d{2}-\d{2}$""",
        message = "startDate must be ISO yyyy-MM-dd",
    )
    val startDate: String? = null, // ISO date format: YYYY-MM-DD

    @field:Pattern(
        regexp = """^\d{4}-\d{2}-\d{2}$""",
        message = "endDate must be ISO yyyy-MM-dd",
    )
    val endDate: String? = null, // ISO date format: YYYY-MM-DD

    val highlights: List<
        @Size(max = 500, message = "Highlight must not exceed 500 characters")
        String,
        >? = null,

    val keywords: List<
        @Size(max = 50, message = "Keyword must not exceed 50 characters")
        String,
        >? = null,

    val roles: List<
        @Size(max = 100, message = "Role must not exceed 100 characters")
        String,
        >? = null,

    @field:Size(max = 100, message = "Entity must not exceed 100 characters")
    val entity: String? = null,

    @field:Size(max = 50, message = "Type must not exceed 50 characters")
    val type: String? = null
)
