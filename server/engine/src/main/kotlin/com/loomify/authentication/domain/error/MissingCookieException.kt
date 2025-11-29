package com.loomify.authentication.domain.error

/**
 * Custom exception for handling missing cookies in the request.
 */
class MissingCookieException(cookieName: String) :
    RuntimeException("Missing required cookie: $cookieName")
