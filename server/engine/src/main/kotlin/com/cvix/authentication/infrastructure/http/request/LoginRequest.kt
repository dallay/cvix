package com.cvix.authentication.infrastructure.http.request

import com.cvix.common.domain.vo.credential.Credential
import com.cvix.common.domain.vo.credential.CredentialValue
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
 * `@property` email User's email address used as login identifier.
 * `@property` password User's password (must meet security requirements defined by Credential.MIN_LENGTH).
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
    @field:Size(
        min = Credential.MIN_LENGTH,
        max = CredentialValue.MAX_CREDENTIAL_LENGTH,
        message = "Password must be between ${Credential.MIN_LENGTH} " +
            "and ${CredentialValue.MAX_CREDENTIAL_LENGTH} characters",
    )
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}\$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, " +
            "one digit, and one special character",
    )
    @field:Schema(
        description = "User's password (must meet security requirements: " +
            "minimum ${Credential.MIN_LENGTH} and maximum ${CredentialValue.MAX_CREDENTIAL_LENGTH} characters, " +
            "at least one uppercase, one lowercase, one digit, and one special character)",
        example = "MySecureP@ssw0rd",
        required = true,
        format = "password",
        minLength = Credential.MIN_LENGTH,
        maxLength = CredentialValue.MAX_CREDENTIAL_LENGTH,
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
