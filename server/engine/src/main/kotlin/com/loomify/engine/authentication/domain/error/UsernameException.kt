package com.loomify.engine.authentication.domain.error

import com.loomify.common.domain.error.BusinessRuleValidationException

/**
 * Represents an exception that occurs during the authentication process.
 *
 * This class is a sealed class, meaning it can only be extended within the same file.
 * It extends the RuntimeException class which is an unchecked exception.
 */
class UsernameException(override val message: String, override val cause: Throwable? = null) :
    BusinessRuleValidationException(message, cause)
