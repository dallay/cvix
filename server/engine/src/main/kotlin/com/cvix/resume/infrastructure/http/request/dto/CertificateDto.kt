package com.cvix.resume.infrastructure.http.request.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for certificate.
 */
data class CertificateDto(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 100, message = "Name must not exceed 100 characters")
    val name: String,

    val date: String? = null,

    val url: String? = null,

    @field:Size(max = 100, message = "Issuer must not exceed 100 characters")
    val issuer: String? = null
)
