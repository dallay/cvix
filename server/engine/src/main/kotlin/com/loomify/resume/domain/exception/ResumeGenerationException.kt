package com.loomify.resume.domain.exception

/**
 * Base exception for all resume generation-related errors.
 * Follows Hexagonal Architecture - domain layer defines exception hierarchy.
 */
sealed class ResumeGenerationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Thrown when resume data fails validation rules (e.g., missing required sections).
 */
class InvalidResumeDataException(
    message: String,
    cause: Throwable? = null
) : ResumeGenerationException(message, cause)

/**
 * Thrown when LaTeX template rendering fails.
 */
class TemplateRenderingException(
    message: String,
    cause: Throwable? = null
) : ResumeGenerationException(message, cause)

/**
 * Thrown when PDF generation process fails (Docker, LaTeX compilation, etc.).
 */
class PdfGenerationException(
    message: String,
    cause: Throwable? = null
) : ResumeGenerationException(message, cause)

/**
 * Thrown when PDF generation times out (>10 seconds per spec).
 */
class PdfGenerationTimeoutException(
    message: String = "PDF generation exceeded timeout limit (10 seconds)",
    cause: Throwable? = null
) : ResumeGenerationException(message, cause)

/**
 * Thrown when LaTeX injection attempt is detected.
 */
class LaTeXInjectionException(
    message: String = "Potentially malicious LaTeX content detected",
    cause: Throwable? = null
) : ResumeGenerationException(message, cause)
