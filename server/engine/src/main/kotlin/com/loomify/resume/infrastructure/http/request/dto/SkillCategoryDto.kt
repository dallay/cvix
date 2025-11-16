package com.loomify.resume.infrastructure.http.request.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for skill category.
 */
data class SkillCategoryDto(
    @field:NotBlank(message = "Skill category name is required")
    @field:Size(max = 100, message = "Skill category name must not exceed 100 characters")
    val name: String,

    @field:Size(max = 50, message = "Level must not exceed 50 characters")
    val level: String? = null,

    @field:Size(min = 1, message = "At least one skill is required")
    val keywords: List<
        @Size(max = 50, message = "Skill must not exceed 50 characters")
        String,
        >
)
