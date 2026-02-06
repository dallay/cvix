package com.cvix.identity.domain.workspace

import com.cvix.common.domain.error.BusinessRuleValidationException
import com.cvix.common.domain.error.EntityNotFoundException

/**
 * Exception thrown when a workspace-related business rule is violated.
 *
 * @param message The error message.
 * @param cause The cause of the exception.
 */
class WorkspaceException(
    override val message: String,
    override val cause: Throwable? = null
) : BusinessRuleValidationException(message, cause)

/**
 * This class represents a specific exception that is thrown when a Workspace is not found.
 * It extends the EntityNotFoundException class.
 *
 * @property message The detail message string of this throwable.
 * @property cause The cause of this throwable.
 */
data class WorkspaceNotFoundException(
    override val message: String,
    override val cause: Throwable? = null
) : EntityNotFoundException(message, cause)

/**
 * Exception thrown when a user is not authorized to access a workspace.
 *
 * @property message The detail message string of this exception.
 * @property cause The cause of this exception, if available.
 */
data class WorkspaceAuthorizationException(
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)
