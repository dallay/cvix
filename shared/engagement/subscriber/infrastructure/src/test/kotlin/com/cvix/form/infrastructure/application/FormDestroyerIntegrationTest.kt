package com.cvix.form.infrastructure.application

import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.config.DatabaseTestContainers
import com.cvix.config.TestDatabaseConfiguration
import com.cvix.config.TestSecurityConfiguration
import com.cvix.form.application.delete.FormDestroyer
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.event.SubscriptionFormDeletedEvent
import com.cvix.form.infrastructure.TestSubscriptionFormApplication
import com.cvix.form.infrastructure.persistence.entity.SubscriptionFormEntity
import com.cvix.spring.boot.infrastructure.persistence.outbox.OutboxEntity
import io.mockk.mockk
import java.util.*
import kotlinx.coroutines.reactor.awaitSingleOrNull
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
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase

@SpringBootTest(
    classes = [
        TestSubscriptionFormApplication::class,
        TestSecurityConfiguration::class,
        TestDatabaseConfiguration::class,
    ],
)
@Sql(
    scripts = ["/db/subscription/subscription_forms.sql"],
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
)
@Sql(
    scripts = ["/db/subscription/clean.sql"],
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
)
class FormDestroyerIntegrationTest : DatabaseTestContainers() {

    @Autowired
    private lateinit var formDestroyer: FormDestroyer

    @Autowired
    private lateinit var entityTemplate: R2dbcEntityTemplate

    @TestConfiguration
    class Config {
        @Bean
        fun subscriptionFormDeletedEventPublisher(): EventPublisher<SubscriptionFormDeletedEvent> =
            mockk(relaxed = true)
    }

    @Test
    fun `should delete form and create outbox entry`() = runTest {
        // Arrange: use fixed IDs created by SQL fixture
        val formId = SubscriptionFormId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        val workspaceId = WorkspaceId(UUID.fromString("22222222-2222-2222-2222-222222222222"))

        // Act
        formDestroyer.delete(workspaceId, formId)

        // Assert: Form is deleted
        val deletedForm = entityTemplate.selectOne(
            query(where("id").`is`(formId.value)),
            SubscriptionFormEntity::class.java,
        ).awaitSingleOrNull()
        assertThat(deletedForm).isNull()

        // Assert: Outbox entry is created
        val outboxEntry = entityTemplate.selectOne(
            query(where("aggregate_id").`is`(formId.value.toString())),
            OutboxEntity::class.java,
        ).awaitSingleOrNull()

        assertThat(outboxEntry).isNotNull
        assertThat(outboxEntry?.aggregateType).isEqualTo("SubscriptionForm")
        assertThat(outboxEntry?.eventType).isEqualTo("SubscriptionFormDeletedEvent")
        assertThat(outboxEntry?.status).isEqualTo("PENDING")
    }
}
