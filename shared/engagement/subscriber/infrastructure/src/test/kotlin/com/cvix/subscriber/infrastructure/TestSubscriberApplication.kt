package com.cvix.subscriber.infrastructure

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(scanBasePackages = ["com.cvix"])
@ConfigurationPropertiesScan(basePackages = ["com.cvix"])
@EnableR2dbcRepositories(basePackages = ["com.cvix"])
open class TestSubscriberApplication
