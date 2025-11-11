package com.loomify.resume.infrastructure.web.request.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for interest.
 */
data class InterestDto(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 100, message = "Name must not exceed 100 characters")
    val name: String,

    val keywords: List<
        @Size(max = 50, message = "Keyword must not exceed 50 characters")
        String,
        >? = null
)
