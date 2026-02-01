package com.cvix.subscriber.infrastructure

import com.cvix.common.domain.Service
import com.cvix.common.domain.security.Hasher
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.common.infrastructure.security.Sha256Hasher
import io.mockk.mockk
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
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
            classes = [Service::class],
        ),
    ],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = [".*Test.*Application"],
        ),
        ComponentScan.Filter(
            type = FilterType.ANNOTATION,
            classes = [RestController::class, Controller::class],
        ),
    ],
)
open class TestSubscriberApplication {
    @Bean
    open fun workspaceAuthorization(): WorkspaceAuthorization = mockk(relaxUnitFun = true)

    @Bean
    open fun hasher(): Hasher = Sha256Hasher()
}
