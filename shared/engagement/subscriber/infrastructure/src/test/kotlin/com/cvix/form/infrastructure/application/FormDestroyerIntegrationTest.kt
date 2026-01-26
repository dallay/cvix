package com.cvix.form.infrastructure.application

import com.cvix.authentication.infrastructure.TestSecurityConfiguration
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.config.InfrastructureTestContainers
import com.cvix.form.application.delete.FormDestroyer
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormStatus
import com.cvix.form.domain.event.SubscriptionFormDeletedEvent
import com.cvix.form.infrastructure.TestSubscriptionFormApplication
import com.cvix.form.infrastructure.persistence.entity.SubscriptionFormEntity
import com.cvix.spring.boot.infrastructure.persistence.outbox.OutboxEntity
import io.mockk.mockk
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import java.time.Instant

@SpringBootTest(classes = [TestSubscriptionFormApplication::class, TestSecurityConfiguration::class])
class FormDestroyerIntegrationTest : InfrastructureTestContainers() {

    @Autowired
    private lateinit var formDestroyer: FormDestroyer

    @Autowired
    private lateinit var entityTemplate: R2dbcEntityTemplate

    @TestConfiguration
    class Config {
        @Bean
        fun subscriptionFormDeletedEventPublisher(): EventPublisher<SubscriptionFormDeletedEvent> = mockk(relaxed = true)
    }

    @Test
    fun `should delete form and create outbox entry`() = runTest {
        // Arrange
        val workspaceId = WorkspaceId.random()
        val formId = SubscriptionFormId.random()
        val entity = SubscriptionFormEntity(
            id = formId.value,
            name = "Test Form",
            description = "Test Description",
            header = "Header",
            inputPlaceholder = "Placeholder",
            buttonText = "Submit",
            buttonColor = "#000000",
            backgroundColor = "#FFFFFF",
            textColor = "#000000",
            buttonTextColor = "#FFFFFF",
            status = SubscriptionFormStatus.ACTIVE,
            workspaceId = workspaceId.value,
            createdAt = Instant.now()
        )
        entityTemplate.insert(entity).awaitFirstOrNull()

        // Act
        formDestroyer.delete(workspaceId, formId)

        // Assert: Form is deleted
        val deletedForm = entityTemplate.selectOne(
            query(where("id").`is`(formId.value)),
            SubscriptionFormEntity::class.java
        ).awaitFirstOrNull()
        assertThat(deletedForm).isNull()

        // Assert: Outbox entry is created
        val outboxEntry = entityTemplate.selectOne(
            query(where("aggregate_id").`is`(formId.value.toString())),
            OutboxEntity::class.java
        ).awaitFirstOrNull()

        assertThat(outboxEntry).isNotNull
        assertThat(outboxEntry?.aggregateType).isEqualTo("SubscriptionForm")
        assertThat(outboxEntry?.eventType).isEqualTo("SubscriptionFormDeletedEvent")
        assertThat(outboxEntry?.status).isEqualTo("PENDING")
    }
}
