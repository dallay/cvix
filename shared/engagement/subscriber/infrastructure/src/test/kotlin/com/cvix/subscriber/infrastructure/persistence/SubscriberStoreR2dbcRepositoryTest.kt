package com.cvix.subscriber.infrastructure.persistence

import com.cvix.IntegrationTest
import com.cvix.common.domain.model.Language
import com.cvix.common.domain.vo.email.Email
import com.cvix.config.InfrastructureTestContainers
import com.cvix.config.TestSecurityConfiguration
import com.cvix.subscriber.domain.Attributes
import com.cvix.subscriber.domain.Subscriber
import com.cvix.subscriber.domain.SubscriberId
import com.cvix.subscriber.domain.SubscriptionSource
import com.cvix.subscriber.infrastructure.TestSubscriberApplication
import com.cvix.subscriber.infrastructure.persistence.repository.SubscriberReactiveR2dbcRepository
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@IntegrationTest
@SpringBootTest(classes = [TestSubscriberApplication::class, TestSecurityConfiguration::class])
internal class SubscriberStoreR2dbcRepositoryTest : InfrastructureTestContainers() {

    @Autowired
    private lateinit var subscriberReactiveR2dbcRepository: SubscriberReactiveR2dbcRepository

    @Autowired
    private lateinit var subscriberStoreR2dbcRepository: SubscriberStoreR2dbcRepository

    private val workspaceId = UUID.randomUUID()

    @BeforeEach
    fun setUp() = runTest {
        subscriberReactiveR2dbcRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() = runTest {
        subscriberReactiveR2dbcRepository.deleteAll()
    }

    @Test
    fun `should create and find subscriber by id`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        val subscriber = Subscriber(
            id = SubscriberId(id),
            email = Email("save@example.com"),
            source = SubscriptionSource("test"),
            sourceRaw = "test",
            language = Language.ENGLISH,
            workspaceId = com.cvix.common.domain.model.WorkspaceId(workspaceId),
        )

        // Act
        subscriberStoreR2dbcRepository.create(subscriber)
        val found = subscriberStoreR2dbcRepository.findById(id)

        // Assert
        assertNotNull(found)
        assertEquals(subscriber.email, found?.email)
        assertEquals(subscriber.workspaceId, found?.workspaceId)
    }

    @Test
    fun `should check existence by email and source`() = runTest {
        // Arrange
        val email = "exists@example.com"
        val source = "web"
        val id = UUID.randomUUID()
        val subscriber = Subscriber(
            id = SubscriberId(id),
            email = Email(email),
            source = SubscriptionSource(source),
            sourceRaw = source,
            language = Language.ENGLISH,
            workspaceId = com.cvix.common.domain.model.WorkspaceId(workspaceId),
        )
        subscriberStoreR2dbcRepository.create(subscriber)

        // Act
        val exists = subscriberStoreR2dbcRepository.existsByEmailAndSource(email, source)
        val notExists = subscriberStoreR2dbcRepository.existsByEmailAndSource("other@example.com", source)

        // Assert
        assertTrue(exists)
        assertTrue(!notExists)
    }

    @Test
    fun `should find by metadata`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        val attributes = Attributes(metadata = mapOf("plan" to "premium", "internal_id" to "123"))
        val subscriber = Subscriber(
            id = SubscriberId(id),
            email = Email("metadata@example.com"),
            source = SubscriptionSource("api"),
            sourceRaw = "api",
            language = Language.ENGLISH,
            attributes = attributes,
            workspaceId = com.cvix.common.domain.model.WorkspaceId(workspaceId),
        )
        subscriberStoreR2dbcRepository.create(subscriber)

        // Act
        val results = subscriberStoreR2dbcRepository.findAllByMetadata("plan", "premium")
        val noResults = subscriberStoreR2dbcRepository.findAllByMetadata("plan", "free")

        // Assert
        assertEquals(1, results.size)
        assertEquals(id, results[0].id.value)
        assertTrue(noResults.isEmpty())
    }

    @Test
    fun `should search by offset`() = runTest {
        // Arrange
        val baseEmail = "offset@example.com"
        (1..5).forEach { i ->
            val subscriber = Subscriber(
                id = SubscriberId(UUID.randomUUID()),
                email = Email("$i$baseEmail"),
                source = SubscriptionSource("web"),
                sourceRaw = "web",
                language = Language.ENGLISH,
                workspaceId = com.cvix.common.domain.model.WorkspaceId(workspaceId),
            )
            subscriberStoreR2dbcRepository.create(subscriber)
        }

        // Act
        val page = subscriberStoreR2dbcRepository.searchAllByOffset(size = 2, page = 0)

        // Assert
        assertEquals(2, page.data.size)
        assertEquals(5, page.total)
        assertEquals(3, page.totalPages)
    }
}
