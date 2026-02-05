package com.cvix.identity.infrastructure

import com.cvix.common.domain.Service
import com.cvix.config.TestSecurityConfiguration
import com.cvix.identity.infrastructure.authentication.OAuth2Configuration
import com.cvix.ratelimit.infrastructure.config.RateLimitConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = ["com.cvix"])
@EnableR2dbcRepositories(basePackages = ["com.cvix"])
@ComponentScan(
    basePackages = ["com.cvix"],
    includeFilters = [
        ComponentScan.Filter(
            type = FilterType.ANNOTATION,
            classes = [Service::class, RestController::class, Controller::class],
        ),
    ],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = [".*Test.*Application"],
        ),
    ],
)
@Import(
    TestSecurityConfiguration::class,
    OAuth2Configuration::class,
    RateLimitConfiguration::class,
)
open class TestIdentityApplication
