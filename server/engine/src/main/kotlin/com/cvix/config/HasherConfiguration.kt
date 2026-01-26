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
        val allowInsecure = hasherSecurityConfig.allowInsecureHasher
        return if (secret.isNotBlank()) {
            log.info("Configuring HmacHasher with provided secret")
            HmacHasher(secret)
        } else {
            if (!allowInsecure) {
                throw IllegalStateException(
                    "No HMAC secret provided and allowInsecureHasher is false. " +
                        "Set ipHmacSecret or allow insecure hasher explicitly.",
                )
            }
            log.warn(
                "No HMAC secret provided. allowInsecureHasher=true, " +
                    "instantiating Sha256Hasher (insecure for anonymization)",
            )
            Sha256Hasher()
        }
    }
}
