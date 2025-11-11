package com.loomify.resume.infrastructure.web.request.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for language proficiency.
 */
data class LanguageDto(
    @field:NotBlank(message = "Language is required")
    @field:Size(max = 50, message = "Language must not exceed 50 characters")
    val language: String,

    @field:NotBlank(message = "Fluency is required")
    @field:Size(max = 50, message = "Fluency must not exceed 50 characters")
    val fluency: String
)
