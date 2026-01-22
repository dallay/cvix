package com.cvix.subscriber.infrastructure.http.response

import com.cvix.common.domain.bus.query.Response
import com.cvix.subscriber.domain.Attributes
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

sealed interface SubscriberApiResponse : Response

/**
 * API Data Transfer Object representing a single subscriber with Swagger documentation.
 */
@Schema(description = "Subscriber data details")
data class SubscriberDataResponse(
    @field:Schema(description = "Subscriber unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: String,

    @field:Schema(description = "Subscriber email address", example = "user@example.com")
    val email: String,

    @field:Schema(description = "Source of the subscription", example = "web")
    val source: String,

    @field:Schema(description = "Current status of the subscriber", example = "ENABLED")
    val status: String,

    @field:Schema(description = "Additional structured attributes")
    val attributes: Attributes? = null,

    @field:Schema(description = "Preferred language code", example = "en")
    val language: String? = null,

    @field:Schema(description = "Hashed IP address for privacy", example = "a1b2c3d4...")
    val ip: String? = null,

    @field:Schema(description = "Subscription confirmation token", example = "token123")
    val confirmationToken: String? = null,

    @field:Schema(description = "Expiration timestamp for the confirmation token")
    val confirmationExpiresAt: Instant? = null,

    @field:Schema(description = "Indicates if the subscriber opted out of contact")
    val doNotContact: Boolean = false,

    @field:Schema(description = "Creation timestamp (ISO-8601)", example = "2024-01-19T10:00:00Z")
    val createdAt: String? = null,

    @field:Schema(description = "Last update timestamp (ISO-8601)", example = "2024-01-19T10:00:00Z")
    val updatedAt: String? = null,
) : SubscriberApiResponse
