package com.cvix.waitlist.infrastructure.http.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Request body for joining the waitlist.
 *
 * @property email User's email address.
 * @property source Source from where the user is joining (e.g., "landing-hero", "landing-cta").
 * @property language User's preferred language code (e.g., "en", "es").
 */
data class JoinWaitlistRequest(
    @field:Email(message = "Email must be valid")
    @field:NotBlank(message = "Email is required")
    @field:Size(max = 320, message = "Email must not exceed 320 characters")
    @field:Schema(
        description = "User's email address",
        example = "user@example.com",
        required = true,
    )
    val email: String,

    @field:NotBlank(message = "Source is required")
    @field:Pattern(regexp = "^(landing-hero|landing-cta|blog-cta|unknown)$", message = "Invalid source")
    @field:Schema(
        description = "Source from where the user is joining",
        example = "landing-hero",
        allowableValues = ["landing-hero", "landing-cta", "blog-cta", "unknown"],
        required = true,
    )
    val source: String,

    @field:NotBlank(message = "Language is required")
    @field:Pattern(regexp = "^(en|es)$", message = "Language must be 'en' or 'es'")
    @field:Schema(
        description = "User's preferred language",
        example = "en",
        allowableValues = ["en", "es"],
        required = true,
    )
    val language: String,
)
