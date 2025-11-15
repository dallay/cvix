package com.loomify.resume.infrastructure.web.request.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for award.
 */
data class AwardDto(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 200, message = "Title must not exceed 200 characters")
    val title: String,

    val date: String? = null,

    @field:Size(max = 100, message = "Awarder must not exceed 100 characters")
    val awarder: String? = null,

    @field:Size(max = 500, message = "Summary must not exceed 500 characters")
    val summary: String? = null
)
