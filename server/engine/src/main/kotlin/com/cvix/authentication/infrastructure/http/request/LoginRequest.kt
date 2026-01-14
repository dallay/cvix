package com.cvix.authentication.infrastructure.http.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Request body for user authentication (login).
 *
 * This request contains the user credentials and an optional flag to extend session duration.
 * The rememberMe flag affects the refresh token TTL (Time To Live).
 *
 * @property email User's email address used as login identifier.
 * @property password User's password (8-100 characters, all character types allowed).
 * @property rememberMe Whether to extend the session duration (affects refresh token TTL).
 * @created 31/7/23
 */
data class LoginRequest(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Email must be a valid email address")
    @field:Size(max = 100, message = "Email must not exceed 100 characters")
    @field:Schema(
        description = "User's email address used as login identifier",
        example = "user@example.com",
        required = true,
        format = "email",
        maxLength = 100,
    )
    val email: String,

    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @field:Pattern(
        regexp = "^.{8,100}\$",
        message = "Password must be between 8 and 100 characters",
    )
    @field:Schema(
        description = "User's password. Must be between 8 and 100 characters. " +
            "All characters including spaces, Unicode, and punctuation are allowed. " +
            "Note: Server-side protections (password breach checks, brute-force throttling, " +
            "account lockout, and MFA) are enforced during authentication.",
        example = "my secure password",
        required = true,
        format = "password",
        minLength = 8,
        maxLength = 100,
    )
    val password: String,

    @field:Schema(
        description = "Whether to extend the session duration. " +
            "If true, the refresh token TTL will be extended (e.g., 30 days instead of 7 days)",
        required = false,
        defaultValue = "false",
    )
    val rememberMe: Boolean = false,
)
