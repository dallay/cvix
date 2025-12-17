package com.cvix.common.domain.error

/**
 * Thrown when mapping from storage or transport (DB, API) to domain model fails due to invalid data.
 * For example, when a DB email field is not a valid Email, or enum parsing fails.
 * Always wrap the underlying cause and log details at the mapping layer.
 */
class DomainMappingException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
