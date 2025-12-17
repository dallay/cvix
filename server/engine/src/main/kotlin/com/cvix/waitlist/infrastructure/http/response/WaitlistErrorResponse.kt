package com.cvix.waitlist.infrastructure.http.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Response body for waitlist errors.
 *
 * @property error Error code identifier.
 * @property message User-friendly error message.
 * @property retryAfter Optional retry-after value in seconds (for rate limiting).
 */
data class WaitlistErrorResponse(
    @field:Schema(description = "Error code", example = "EMAIL_ALREADY_EXISTS")
    val error: String,

    @field:Schema(
        description = "User-friendly error message",
        example = "This email is already on the waitlist",
    )
    val message: String,

    @field:Schema(
        description = "Retry after seconds (for rate limiting)",
        example = "60",
        required = false,
    )
    val retryAfter: Int? = null,
) : JoinWaitlistApiResponse
