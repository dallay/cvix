package com.cvix.contact.infrastructure.http.response

import io.swagger.v3.oas.annotations.media.Schema

sealed interface SendContactApiResponse

/**
 * Response body for successful contact form submission.
 *
 * @property success Indicates if the operation was successful.
 * @property message A user-friendly message.
 */
data class SendContactResponse(
    @field:Schema(description = "Indicates if the operation was successful", example = "true")
    val success: Boolean = true,

    @field:Schema(
        description = "User-friendly success message",
        example = "Thank you for contacting us! We'll get back to you soon.",
    )
    val message: String,
) : SendContactApiResponse
