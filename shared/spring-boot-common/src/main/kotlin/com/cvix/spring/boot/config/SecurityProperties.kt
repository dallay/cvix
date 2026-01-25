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
    val hasher: HasherProperties = HasherProperties(),
)

data class HasherProperties(
    /** Default hasher name to use when none is specified (e.g. "sha256" or "hmac") */
    val default: String = "sha256",

    /** Secret used by HMAC-based hashers (can be empty if not used). */
    val ipHmacSecret: String = "",
)
