package com.cvix.spring.boot.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HasherRegistryValidationTest {

    @Test
    fun `throws when default is hmac but secret is blank`() {
        val props = SecurityProperties(hasher = HasherProperties(default = "hmac", ipHmacSecret = ""))

        assertThrows<IllegalStateException> {
            HasherRegistry(props)
        }
    }
}
