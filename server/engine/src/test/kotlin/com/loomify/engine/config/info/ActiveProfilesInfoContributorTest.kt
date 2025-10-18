package com.loomify.engine.config.info

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.InstanceOfAssertFactories.list
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.info.Info
import org.springframework.mock.env.MockEnvironment

internal class ActiveProfilesInfoContributorTest {

    @Test
    fun activeProfilesShouldBeSetWhenProfilesActivated() {
        val environment = MockEnvironment().apply {
            setActiveProfiles("prod")
            setDefaultProfiles("dev", "api-docs")
        }
        val contributor = ActiveProfilesInfoContributor(environment)
        val builder = Info.Builder()
        contributor.contribute(builder)
        val info = builder.build()

        assertThat(info.get("activeProfiles"))
            .asInstanceOf(list(String::class.java))
            .contains("prod")
    }

    @Test
    fun defaultProfilesShouldBeSetWhenNoProfilesActivated() {
        val environment = MockEnvironment().apply {
            setDefaultProfiles("dev", "api-docs")
        }
        val contributor = ActiveProfilesInfoContributor(environment)
        val builder = Info.Builder()
        contributor.contribute(builder)
        val info = builder.build()

        assertThat(info.get("activeProfiles"))
            .asInstanceOf(list(String::class.java))
            .contains("dev", "api-docs")
    }
}
