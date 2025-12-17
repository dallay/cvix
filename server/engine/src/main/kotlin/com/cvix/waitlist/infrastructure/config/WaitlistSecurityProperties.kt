package com.cvix.waitlist.infrastructure.config

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "waitlist.security")
@Validated
data class WaitlistSecurityProperties(
    @field:NotBlank(message = "waitlist.security.ip-hmac-secret must be configured")
    val ipHmacSecret: String = ""
)
