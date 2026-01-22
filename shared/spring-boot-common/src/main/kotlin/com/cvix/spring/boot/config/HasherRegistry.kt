package com.cvix.spring.boot.config

import com.cvix.common.domain.security.Hasher
import com.cvix.common.domain.security.HmacHasher
import com.cvix.common.domain.security.Sha256Hasher
import org.springframework.stereotype.Component

/** Simple registry + factory for Hasher strategy implementations. */
@Component
class HasherRegistry(private val securityProperties: SecurityProperties) {
    init {
        // Fail fast: if configuration requests HMAC as default but no secret provided,
        // throw during bean construction so application context fails early.
        if (securityProperties.hasher.default == "hmac" && securityProperties.hasher.ipHmacSecret.isBlank()) {
            // use check() to assert configuration preconditions — clearer intent for a config failure
            check(!securityProperties.hasher.ipHmacSecret.isBlank()) {
                "HMAC selected as default hasher but application.security.hasher.ip-hmac-secret is blank"
            }
        }
    }

    private val available: Map<String, Hasher> by lazy {
        mapOf(
            "sha256" to Sha256Hasher(),
            "hmac" to HmacHasher(securityProperties.hasher.ipHmacSecret),
        )
    }

    fun get(name: String?): Hasher {
        val effective = name ?: securityProperties.hasher.default
        return available[effective] ?: available.getValue(securityProperties.hasher.default)
    }
}
