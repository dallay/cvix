package com.cvix.subscription

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = ["com.cvix"])
@EnableR2dbcRepositories(
    basePackages = [
        "com.cvix.subscription.infrastructure",
        "com.cvix.identity.infrastructure.user.persistence.repository",
        "com.cvix.spring.boot.infrastructure.persistence.outbox",
    ],
)
@ComponentScan(
    basePackages = ["com.cvix.subscription", "com.cvix.spring.boot"],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = [".*Test.*Application", ".*TestSecurityConfiguration"],
        ),
    ],
)
open class TestSubscriptionApplication
