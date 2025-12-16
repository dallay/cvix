package com.cvix.waitlist.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for waitlist security.
 *
 * @property ipHmacSecret The secret key used for HMAC-SHA256 hashing of IP addresses.
 * Must be kept secret to prevent rainbow table attacks on the hashed IPs.
 */
@ConfigurationProperties(prefix = "waitlist.security")
data class WaitlistSecurityProperties(
    val ipHmacSecret: String = ""
)
