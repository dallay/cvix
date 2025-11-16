package com.loomify.resume.infrastructure.http.request.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for publication.
 */
data class PublicationDto(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 200, message = "Name must not exceed 200 characters")
    val name: String,

    @field:Size(max = 100, message = "Publisher must not exceed 100 characters")
    val publisher: String? = null,

    val releaseDate: String? = null,

    val url: String? = null,

    @field:Size(max = 500, message = "Summary must not exceed 500 characters")
    val summary: String? = null
)
