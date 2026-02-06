package com.cvix.resume

import com.cvix.common.domain.Service
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
@EnableR2dbcRepositories(
    basePackages = ["com.cvix.resume.infrastructure", "com.cvix.spring.boot.infrastructure.persistence.outbox"],
)
@ComponentScan(
    basePackages = ["com.cvix.resume", "com.cvix.spring.boot", "com.cvix.controllers", "com.cvix.config"],
    includeFilters = [
        ComponentScan.Filter(
            type = FilterType.ANNOTATION,
            classes = [Service::class, RestController::class, Controller::class],
        ),
    ],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = [".*Test.*Application", ".*TestSecurityConfiguration"],
        ),
    ],
)
@Import(
    TestSubscriptionResolverConfiguration::class,
    TestWorkspaceAuthorizationConfiguration::class,
)
open class TestResumeApplication
