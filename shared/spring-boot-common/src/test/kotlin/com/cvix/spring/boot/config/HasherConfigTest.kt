package com.cvix.spring.boot.config

import com.cvix.common.domain.security.HasherSecurityConfig
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class HasherConfigTest {

    @Test
    fun `implements HasherSecurityConfig and exposes secret`() {
        val props = SecurityProperties(ipHmacSecret = "top-secret")
        val cfg = HasherConfig(props)

        (cfg as HasherSecurityConfig).ipHmacSecret shouldBe "top-secret"
        (cfg as HasherSecurityConfig).allowInsecureHasher shouldBe false
    }
}
