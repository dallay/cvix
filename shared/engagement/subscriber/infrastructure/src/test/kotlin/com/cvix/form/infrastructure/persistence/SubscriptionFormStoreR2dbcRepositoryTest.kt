package com.cvix.form.infrastructure.persistence

import com.cvix.authentication.infrastructure.TestSecurityConfiguration
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.config.InfrastructureTestContainers
import com.cvix.form.application.SubscriberFormStub
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.infrastructure.TestSubscriptionFormApplication
import com.cvix.form.infrastructure.persistence.repository.SubscriptionFormReactiveR2dbcRepository
import com.cvix.subscriber.infrastructure.persistence.config.SubscriberR2dbcConfig
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [
        TestSubscriptionFormApplication::class,
        TestSecurityConfiguration::class,
        SubscriberR2dbcConfig::class,
    ],
)
internal class SubscriptionFormStoreR2dbcRepositoryTest : InfrastructureTestContainers() {

    @Autowired
    private lateinit var subscriptionFormReactiveR2dbcRepository: SubscriptionFormReactiveR2dbcRepository

    @Autowired
    private lateinit var subscriptionFormStoreR2dbcRepository: SubscriptionFormStoreR2dbcRepository

    @BeforeEach
    fun setUp() = runTest {
        subscriptionFormReactiveR2dbcRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() = runTest {
        subscriptionFormReactiveR2dbcRepository.deleteAll()
    }

    @Test
    fun `should create and find subscription form by id`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        val form = SubscriberFormStub.randomForm(id = SubscriptionFormId(id))

        // Act
        subscriptionFormStoreR2dbcRepository.create(form)
        val found = subscriptionFormStoreR2dbcRepository.findById(SubscriptionFormId(id))

        // Assert
        assertNotNull(found)
        assertEquals(form.name, found?.name)
        assertEquals(form.workspaceId, found?.workspaceId)
        assertEquals(form.settings.content.headerTitle, found?.settings?.content?.headerTitle)
    }

    @Test
    fun `should find by form id and workspace id`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        val workspaceId = WorkspaceId.random()
        val form = SubscriberFormStub.randomForm(id = SubscriptionFormId(id), workspaceId = workspaceId)
        subscriptionFormStoreR2dbcRepository.create(form)

        // Act
        val found = subscriptionFormStoreR2dbcRepository.findByFormIdAndWorkspaceId(SubscriptionFormId(id), workspaceId)

        // Assert
        assertNotNull(found)
        assertEquals(id, found?.id?.value)
        assertEquals(workspaceId, found?.workspaceId)
    }

    @Test
    fun `should delete a form`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        val form = SubscriberFormStub.randomForm(id = SubscriptionFormId(id))
        subscriptionFormStoreR2dbcRepository.create(form)

        // Act
        subscriptionFormStoreR2dbcRepository.delete(SubscriptionFormId(id))
        val found = subscriptionFormStoreR2dbcRepository.findById(SubscriptionFormId(id))

        // Assert
        assertNull(found)
    }
}
