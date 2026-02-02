package com.cvix.form.infrastructure.persistence.mapper

/**
 * Exception thrown when a domain mapping operation fails.
 *
 * @param message The error message describing the mapping failure.
 * @param cause The underlying cause of the mapping failure, if any.
 */
class DomainMappingException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    /**
     * Secondary constructor that accepts only a message.
     *
     * @param message The error message describing the mapping failure.
     */
    constructor(message: String) : this(message, null)
}
