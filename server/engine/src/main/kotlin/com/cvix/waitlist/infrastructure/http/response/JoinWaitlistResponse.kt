package com.cvix.waitlist.infrastructure.http.response

import io.swagger.v3.oas.annotations.media.Schema

sealed interface JoinWaitlistApiResponse

/**
 * Response body for successful waitlist join.
 *
 * @property success Indicates if the operation was successful.
 * @property message A user-friendly message.
 */
data class JoinWaitlistResponse(
    @field:Schema(description = "Indicates if the operation was successful", example = "true")
    val success: Boolean = true,

    @field:Schema(
        description = "User-friendly success message",
        example = "You've been added to the waitlist!",
    )
    val message: String,
) : JoinWaitlistApiResponse
