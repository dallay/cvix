package com.cvix.resume.infrastructure.http.request.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for social profile per JSON Resume Schema.
 */
data class ProfileDto(
    @field:NotBlank(message = "Network is required")
    @field:Size(max = 50, message = "Network must not exceed 50 characters")
    val network: String,

    @field:Size(max = 100, message = "Username must not exceed 100 characters")
    val username: String? = null,

    @field:NotBlank(message = "URL is required")
    val url: String
)
