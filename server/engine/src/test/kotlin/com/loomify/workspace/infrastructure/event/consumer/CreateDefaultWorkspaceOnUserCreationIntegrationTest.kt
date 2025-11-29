package com.loomify.workspace.infrastructure.event.consumer

import com.loomify.IntegrationTest
import com.loomify.common.domain.bus.event.EventPublisher
import com.loomify.config.InfrastructureTestContainers
import com.loomify.users.domain.UserId
import com.loomify.users.domain.event.UserCreatedEvent
import com.loomify.workspace.domain.WorkspaceFinderRepository
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql

@IntegrationTest
class CreateDefaultWorkspaceOnUserCreationIntegrationTest : InfrastructureTestContainers() {

    @Autowired
    private lateinit var eventPublisher: EventPublisher<UserCreatedEvent>

    @Autowired
    private lateinit var workspaceFinderRepository: WorkspaceFinderRepository

    private val faker = Faker()

    @BeforeEach
    fun setUp() {
        startInfrastructure()
    }

    @Test
    @Sql("/db/user/users.sql")
    @Sql("/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should create default workspace when user is created and has no existing workspaces`() = runTest {
        // Given
        val userId = UUID.fromString("efc4b2b8-08be-4020-93d5-f795762bf5c9")
        val firstname = faker.name().firstName()
        val lastname = faker.name().lastName()
        val email = faker.internet().emailAddress()

        val userCreatedEvent = UserCreatedEvent(id = userId, email = email, firstName = firstname, lastName = lastname)

        // When
        eventPublisher.publish(userCreatedEvent)

        // Then
        eventually(5.seconds) {
            val workspaces = workspaceFinderRepository.findByOwnerId(UserId(userId))
            workspaces shouldHaveSize 1

            val workspace = workspaces.first()
            workspace.name shouldBe "$firstname $lastname's Workspace"
            workspace.description shouldBe "Default workspace created automatically upon user registration"
            workspace.ownerId.id shouldBe userId
        }
    }

    @Test
    @Sql("/db/user/users.sql")
    @Sql("/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should create default workspace with firstname only when lastname is null`() = runTest {
        // Given
        val userId = UUID.fromString("efc4b2b8-08be-4020-93d5-f795762bf5c9")
        val firstname = faker.name().firstName()
        val email = faker.internet().emailAddress()

        val userCreatedEvent = UserCreatedEvent(id = userId, email = email, firstName = firstname, lastName = null)

        // When
        eventPublisher.publish(userCreatedEvent)

        // Then
        eventually(5.seconds) {
            val workspaces = workspaceFinderRepository.findByOwnerId(UserId(userId))
            workspaces shouldHaveSize 1
            workspaces.first().name shouldBe "$firstname's Workspace"
        }
    }

    @Test
    @Sql("/db/user/users.sql")
    @Sql("/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should create default workspace with lastname only when firstname is null`() = runTest {
        // Given
        val userId = UUID.fromString("efc4b2b8-08be-4020-93d5-f795762bf5c9")
        val lastname = faker.name().lastName()
        val email = faker.internet().emailAddress()

        val userCreatedEvent = UserCreatedEvent(id = userId, email = email, firstName = null, lastName = lastname)

        // When
        eventPublisher.publish(userCreatedEvent)

        // Then
        eventually(5.seconds) {
            val workspaces = workspaceFinderRepository.findByOwnerId(UserId(userId))
            workspaces shouldHaveSize 1
            workspaces.first().name shouldBe "$lastname's Workspace"
        }
    }

    @Test
    @Sql("/db/user/users.sql")
    @Sql("/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should create default workspace with 'My Workspace' when both names are null`() = runTest {
        // Given
        val userId = UUID.fromString("efc4b2b8-08be-4020-93d5-f795762bf5c9")
        val email = faker.internet().emailAddress()

        val userCreatedEvent = UserCreatedEvent(id = userId, email = email, firstName = null, lastName = null)

        // When
        eventPublisher.publish(userCreatedEvent)

        // Then
        eventually(5.seconds) {
            workspaceFinderRepository.findByOwnerId(UserId(userId)).first().name shouldBe "My Workspace"
        }
    }

    @Test
    @Sql("/db/user/users.sql")
    @Sql("/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should not create workspace when user already has existing workspaces`() = runTest {
        // Given
        val userId = UUID.fromString("efc4b2b8-08be-4020-93d5-f795762bf5c9")
        val userCreatedEvent = UserCreatedEvent(
            id = userId,
            email = faker.internet().emailAddress(),
            firstName = faker.name().firstName(),
            lastName = faker.name().lastName(),
        )

        // Publish the event and wait for the first workspace to be created
        eventPublisher.publish(userCreatedEvent)
        eventually(5.seconds) {
            workspaceFinderRepository.findByOwnerId(UserId(userId)) shouldHaveSize 1
        }

        // When - we publish the same event again
        eventPublisher.publish(userCreatedEvent)

        // Then - we assert that the count remains 1, even after waiting a bit
        eventually(2.seconds) {
            workspaceFinderRepository.findByOwnerId(UserId(userId)) shouldHaveSize 1
        }
    }

    @Test
    @Sql("/db/user/users.sql")
    @Sql("/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should handle workspace names with special characters and whitespace correctly`() = runTest {
        // Given
        val userId = UUID.fromString("efc4b2b8-08be-4020-93d5-f795762bf5c9")
        val userCreatedEvent = UserCreatedEvent(
            id = userId,
            email = faker.internet().emailAddress(),
            firstName = "  José María  ",
            lastName = "  González-López  ",
        )

        // When
        eventPublisher.publish(userCreatedEvent)

        // Then
        eventually(5.seconds) {
            val workspaces = workspaceFinderRepository.findByOwnerId(UserId(userId))
            workspaces shouldHaveSize 1
            workspaces.first().name shouldBe "José María González-López's Workspace"
        }
    }

    @Test
    @Sql("/db/user/users.sql")
    @Sql("/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `should create only one workspace when duplicate user-created events are published concurrently`() = runTest {
        // Given
        val userId = UUID.fromString("efc4b2b8-08be-4020-93d5-f795762bf5c9")
        val event = UserCreatedEvent(
            id = userId,
            email = faker.internet().emailAddress(),
            firstName = faker.name().firstName(),
            lastName = faker.name().lastName(),
        )

        // When - publish both events concurrently
        awaitAll(
            async { eventPublisher.publish(event) },
            async { eventPublisher.publish(event) },
        )

        // Then - only one workspace should exist after both are processed
        eventually(5.seconds) {
            workspaceFinderRepository.findByOwnerId(UserId(userId)) shouldHaveSize 1
        }
    }
}
