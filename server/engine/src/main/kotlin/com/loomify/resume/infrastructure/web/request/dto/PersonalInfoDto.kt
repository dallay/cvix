package com.loomify.resume.infrastructure.web.request.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for personal information.
 */
data class PersonalInfoDto(
    @field:NotBlank(message = "Full name is required")
    @field:Size(max = 100, message = "Full name must not exceed 100 characters")
    val fullName: String,

    @field:Size(max = 100, message = "Label must not exceed 100 characters")
    val label: String? = null,

    val image: String? = null,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Phone is required")
    val phone: String,

    @field:Valid
    val location: LocationDto? = null,

    @field:Valid
    val profiles: List<ProfileDto>? = null,

    // Deprecated: Use profiles array instead. Kept for backward compatibility.
    val linkedin: String? = null,
    val github: String? = null,
    val website: String? = null,

    @field:Size(max = 500, message = "Summary must not exceed 500 characters")
    val summary: String? = null
)
