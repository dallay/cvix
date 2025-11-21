package com.loomify.resume.domain.exception

/**
 * Base exception for all resume-related errors.
 * Follows Hexagonal Architecture - domain layer defines exception hierarchy.
 */
sealed class ResumeException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Exception thrown when a resume is not found or user is unauthorized.
 */
class ResumeNotFoundException(message: String) : ResumeException(message)
