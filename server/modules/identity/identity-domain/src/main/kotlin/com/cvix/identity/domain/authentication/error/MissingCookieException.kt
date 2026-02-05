package com.cvix.identity.domain.authentication.error

/**
 * Custom exception for handling missing cookies in the request.
 */
class MissingCookieException(cookieName: String) :
    RuntimeException("Missing required cookie: $cookieName")
