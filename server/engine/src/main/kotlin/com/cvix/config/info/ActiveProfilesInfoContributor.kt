package com.cvix.config.info

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.stereotype.Component

/**
 * An [InfoContributor] that exposes the list of active Spring profiles.
 */
@Component
class ActiveProfilesInfoContributor(
    environment: ConfigurableEnvironment
) : InfoContributor {
    private val profiles: List<String> =
        if (environment.activeProfiles.isEmpty()) {
            environment.defaultProfiles.toList()
        } else {
            environment.activeProfiles.toList()
        }

    override fun contribute(builder: Info.Builder) {
        builder.withDetail(ACTIVE_PROFILES, profiles)
    }

    companion object {
        private const val ACTIVE_PROFILES = "activeProfiles"
    }
}
