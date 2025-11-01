package com.loomify.resume.infrastructure.web.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

/**
 * Standardized error response for resume generation API.
 * Follows RFC 7807 Problem Details for HTTP APIs structure.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    /**
     * HTTP status code (e.g., 400, 422, 429, 500, 504)
     */
    val status: Int,

    /**
     * Machine-readable error code for client-side handling
     */
    val code: String,

    /**
     * Human-readable error message (localized based on Accept-Language)
     */
    val message: String,

    /**
     * Timestamp of the error
     */
    val timestamp: Instant = Instant.now(),

    /**
     * Request path where error occurred
     */
    val path: String? = null,

    /**
     * Additional error details (e.g., validation errors per field)
     */
    val errors: List<FieldError>? = null,

    /**
     * For rate limiting errors: seconds until user can retry
     */
    val retryAfterSeconds: Long? = null
)

/**
 * Field-level validation error detail.
 */
data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null
)
