package com.cvix.config

import com.cvix.common.domain.security.Hasher
import com.cvix.common.domain.security.HasherSecurityConfig
import com.cvix.common.domain.security.HmacHasher
import com.cvix.common.domain.security.Sha256Hasher
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HasherConfiguration {

    private val log = LoggerFactory.getLogger(HasherConfiguration::class.java)

    @Bean
    fun hasher(hasherSecurityConfig: HasherSecurityConfig): Hasher {
        val secret = hasherSecurityConfig.ipHmacSecret
        return if (secret.isNotBlank()) {
            log.info("Configuring HmacHasher with provided secret")
            HmacHasher(secret)
        } else {
            log.warn("No HMAC secret provided. Falling back to simple Sha256Hasher (insecure for anonymization)")
            Sha256Hasher()
        }
    }
}
