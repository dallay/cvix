package com.loomify.authentication.infrastructure.http.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Data class representing a login request.
 *
 * This class is used for encapsulating the email, password and rememberMe flag
 * required for authenticating a user's login.
 *
 * @property email The email address provided by the user.
 * @property password The password provided by the user.
 * @property rememberMe Whether to extend the session duration (affects refresh token TTL).
 * @created 31/7/23
 */
data class LoginRequest(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Email must be a valid email address")
    @field:Size(max = 100, message = "Email must not exceed 100 characters")
    val email: String,
    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    val password: String,
    val rememberMe: Boolean = false
)
