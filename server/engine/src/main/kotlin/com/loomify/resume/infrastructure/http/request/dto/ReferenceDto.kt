package com.loomify.resume.infrastructure.http.request.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for reference.
 */
data class ReferenceDto(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 100, message = "Name must not exceed 100 characters")
    val name: String,

    @field:Size(max = 500, message = "Reference must not exceed 500 characters")
    val reference: String? = null
)
