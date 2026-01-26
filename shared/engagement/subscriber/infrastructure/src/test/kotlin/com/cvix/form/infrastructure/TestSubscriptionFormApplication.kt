package com.cvix.form.infrastructure

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.form.domain.event.SubscriptionFormDeletedEvent
import io.mockk.mockk
import com.cvix.form.application.delete.FormDestroyer
import com.cvix.spring.boot.infrastructure.persistence.outbox.R2dbcOutboxRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(scanBasePackages = ["com.cvix"])
@ConfigurationPropertiesScan(basePackages = ["com.cvix"])
@EnableR2dbcRepositories(basePackages = ["com.cvix"])
@ComponentScan(
    includeFilters = [
        ComponentScan.Filter(
            type = FilterType.ANNOTATION,
            classes = [Service::class],
        ),
    ],
)
@Import(FormDestroyer::class, R2dbcOutboxRepository::class)
open class TestSubscriptionFormApplication {
    @Bean
    fun subscriptionFormDeletedEventPublisher(): EventPublisher<SubscriptionFormDeletedEvent> = mockk(relaxed = true)
}
