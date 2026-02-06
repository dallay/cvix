package com.cvix.identity.infrastructure.user.http.request

import com.cvix.identity.application.user.register.RegisterUserCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Request body for user registration.
 *
 * This request contains the data required to create a new user account.
 * All fields are mandatory and must meet the specified validation constraints.
 *
 * @property email User's email address (must be unique in the system).
 * @property password User's password (must meet security requirements).
 * @property firstname User's first name.
 * @property lastname User's last name.
 * @created 2/7/23
 */
data class RegisterUserRequest(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Email must be valid")
    @field:Size(max = 255, message = "Email must be less than 255 characters")
    @field:Schema(
        description = "User's email address (must be unique in the system). " +
            "Used as the primary login identifier",
        example = "john.doe@example.com",
        required = true,
        format = "email",
        maxLength = 255,
    )
    val email: String,

    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @field:Schema(
        description = "User's password. Must meet security requirements: " +
            "minimum 8 characters, at least one uppercase letter, one lowercase letter, " +
            "one digit, and one special character",
        example = "MySecureP@ssw0rd",
        required = true,
        format = "password",
        minLength = 8,
        maxLength = 100,
    )
    val password: String,

    @field:NotBlank(message = "Firstname cannot be blank")
    @field:Size(min = 1, max = 100, message = "Firstname must be between 1 and 100 characters")
    @field:Schema(
        description = "User's first name",
        example = "John",
        required = true,
        minLength = 1,
        maxLength = 100,
    )
    val firstname: String,

    @field:NotBlank(message = "Lastname cannot be blank")
    @field:Size(min = 1, max = 100, message = "Lastname must be between 1 and 100 characters")
    @field:Schema(
        description = "User's last name",
        example = "Doe",
        required = true,
        minLength = 1,
        maxLength = 100,
    )
    val lastname: String,
) {
    /**
     * Converts the request to a RegisterUserCommand for processing.
     *
     * @return RegisterUserCommand with the data from this request.
     */
    fun toRegisterUserCommand(): RegisterUserCommand =
        RegisterUserCommand(
            email = email,
            password = password,
            firstname = firstname,
            lastname = lastname,
        )
}
