package com.cvix.subscriber.infrastructure.http.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.UUID

/**
 * Request body for creating or updating a subscriber.
 *
 * @property email User's email address. Must be valid, not blank, and up to 320 characters.
 * @property source Source from where the user is joining (e.g., landing-hero, landing-cta, blog-cta).
 *                  Must be lowercase letters, numbers, or hyphens, 1-50 characters, and not blank.
 *                  Unknown values will be normalized to 'unknown'.
 * @property language User's preferred language. Must be 'en' or 'es', and not blank.
 * @property formId ID of the form used for capture.
 * @property metadata Additional metadata for the subscriber.
 */
data class SubscriberRequest(
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
    @field:Size(min = 1, max = 50, message = "Source must be between 1 and 50 characters")
    @field:Pattern(
        regexp = "^[a-z0-9-]+$",
        message = "Source must contain only lowercase letters, numbers, and hyphens",
    )
    @field:Schema(
        description = "Source from where the user is joining " +
            "(e.g., landing-hero, landing-cta, blog-cta). Unknown values will be normalized to 'unknown'.",
        example = "landing-hero",
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

    @field:Schema(
        description = "ID of the form used for capture",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = false,
    )
    val formId: UUID? = null,

    @field:Schema(
        description = "Additional metadata for the subscriber",
        required = false,
    )
    val metadata: Map<String, String>? = emptyMap(),
)
