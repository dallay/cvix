package com.cvix.spring.boot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * Application security properties for hasher configuration.
 *
 * Binds properties under `application.hasher.*` so other Spring beans can adapt behaviour
 * (for example which hasher to use and HMAC secret).
 */
@ConfigurationProperties(prefix = "application.hasher")
@Validated
data class SecurityProperties(
    /** Default hasher name to use when none is specified (e.g. "sha256" or "hmac") */
    val default: String = "sha256",

    /** Secret used by HMAC-based hashers (can be empty if not used). */
    val ipHmacSecret: String = "",

    /**
     * Allow using an insecure fallback hasher (e.g. plain SHA-256) when no HMAC secret
     * is provided. This should be false in production to avoid accidental insecure
     * anonymization. Bindable via `application.hasher.allow-insecure-hasher`.
     */
    val allowInsecureHasher: Boolean = false,
)
