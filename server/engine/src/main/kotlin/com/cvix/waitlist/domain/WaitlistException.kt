package com.cvix.waitlist.domain

/**
 * Base exception for waitlist-related domain errors.
 */
sealed class WaitlistException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Exception thrown when an email already exists in the waitlist.
 */
class EmailAlreadyExistsException(email: String) :
    WaitlistException("Email already exists in waitlist: $email")

/**
 * Exception thrown when a waitlist entry is not found.
 */
class WaitlistEntryNotFoundException(id: String) :
    WaitlistException("Waitlist entry not found: $id")
