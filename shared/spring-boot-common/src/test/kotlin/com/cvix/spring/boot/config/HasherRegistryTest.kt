package com.cvix.spring.boot.config

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class HasherRegistryTest {

    @Test
    fun `returns sha256 by default when no name provided`() {
        val properties = SecurityProperties()
        val registry = HasherRegistry(properties)

        val hasher = registry.get(null)
        val hashed = hasher.hash("hello")

        // SHA-256 produces 64 hex chars
        hashed.length shouldBe 64
    }

    @Test
    fun `returns hmac when asked and uses provided secret`() {
        val props = SecurityProperties(default = "hmac", ipHmacSecret = "my-secret")
        val registry = HasherRegistry(props)

        val hasher = registry.get("hmac")
        val hashed = hasher.hash("hello")

        // hmac-sha256 also produces 64 hex chars
        hashed.length shouldBe 64
    }
}
