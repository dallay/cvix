package com.cvix.resume.domain.exception

/**
 * Base exception for all resume-related errors.
 * Follows Hexagonal Architecture - domain layer defines exception hierarchy.
 */
sealed class ResumeException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Exception thrown when a resume is not found.
 * Used to signal HTTP 404 Not Found.
 */
class ResumeNotFoundException(message: String) : ResumeException(message)

/**
 * Exception thrown when access to a resume is forbidden (unauthorized or forbidden).
 * Used to signal HTTP 403 Forbidden or 401 Unauthorized.
 */
class ResumeAccessDeniedException(message: String) : ResumeException(message)
