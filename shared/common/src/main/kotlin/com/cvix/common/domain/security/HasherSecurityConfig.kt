package com.cvix.common.domain.security

/**
 * Configuration interface for security-related hashing operations.
 *
 * Implementations of this interface should provide the secret key used for HMAC operations
 * involving IP addresses or similar sensitive data.
 *
 * @property ipHmacSecret The secret key used for HMAC hashing of IP addresses.
 *
 * @created 20/1/26
 */
interface HasherSecurityConfig {
    val ipHmacSecret: String

    /**
     * Whether to allow an insecure fallback hasher when no HMAC secret is configured.
     * Implementations should default to false for safety.
     */
    val allowInsecureHasher: Boolean
}
