package com.cvix.waitlist.infrastructure.config

import com.cvix.common.domain.security.Hasher
import com.cvix.spring.boot.config.HasherRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Infrastructure wiring for Waitlist-specific beans.
 * Exposes a Hasher bean derived from the project's HasherRegistry so the application
 * layer can depend on the Hasher interface without referencing infra details.
 */
@Configuration
class WaitlistHasherConfiguration(private val hasherRegistry: HasherRegistry) {
    @Bean
    fun waitlistHasher(): Hasher = hasherRegistry.get(null)
}
