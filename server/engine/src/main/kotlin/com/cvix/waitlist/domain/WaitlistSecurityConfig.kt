package com.cvix.waitlist.domain

/**
 * Port for accessing waitlist security configuration.
 *
 * This interface provides access to security-related configuration
 * without coupling the application layer to infrastructure concerns.
 */
interface WaitlistSecurityConfig {

    /**
     * HMAC secret for IP address hashing.
     * Used to anonymize user IP addresses for privacy compliance.
     */
    val ipHmacSecret: String
}
