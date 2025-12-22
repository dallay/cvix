package com.cvix.waitlist.infrastructure.config

import com.cvix.waitlist.domain.WaitlistSecurityConfig
import org.springframework.stereotype.Component

/**
 * Spring configuration adapter implementing the WaitlistSecurityConfig port.
 *
 * This adapter bridges the Spring @ConfigurationProperties to the domain port,
 * allowing the application layer to remain framework-agnostic.
 */
@Component
class WaitlistSecurityConfiguration(
    private val properties: WaitlistSecurityProperties
) : WaitlistSecurityConfig {

    override val ipHmacSecret: String
        get() = properties.ipHmacSecret
}
