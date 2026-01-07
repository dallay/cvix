package com.cvix.contact.infrastructure.http.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Request body for sending a contact form submission.
 *
 * @property name Sender's full name.
 * @property email Sender's email address.
 * @property subject Message subject.
 * @property message Message content.
 * @property hcaptchaToken hCaptcha verification token from the client.
 */
data class SendContactRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @field:Schema(
        description = "Sender's full name",
        example = "John Doe",
        required = true,
    )
    val name: String,

    @field:Email(message = "Email must be valid")
    @field:NotBlank(message = "Email is required")
    @field:Size(max = 320, message = "Email must not exceed 320 characters")
    @field:Schema(
        description = "Sender's email address",
        example = "john@example.com",
        required = true,
    )
    val email: String,

    @field:NotBlank(message = "Subject is required")
    @field:Size(min = 3, max = 200, message = "Subject must be between 3 and 200 characters")
    @field:Schema(
        description = "Message subject",
        example = "Question about your services",
        required = true,
    )
    val subject: String,

    @field:NotBlank(message = "Message is required")
    @field:Size(min = 10, max = 5000, message = "Message must be between 10 and 5000 characters")
    @field:Schema(
        description = "Message content",
        example = "I would like to know more about...",
        required = true,
    )
    val message: String,

    @field:NotBlank(message = "hCaptcha token is required")
    @field:Schema(
        description = "hCaptcha verification token from the client",
        example = "P0_eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
        required = true,
    )
    val hcaptchaToken: String
)
