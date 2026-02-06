package com.cvix.identity.infrastructure.workspace.event.consumer

import com.cvix.UnitTest
import com.cvix.common.domain.bus.Mediator
import com.cvix.common.domain.bus.command.CommandHandlerExecutionError
import com.cvix.identity.application.workspace.create.CreateWorkspaceCommand
import com.cvix.identity.domain.user.event.UserCreatedEvent
import io.kotest.common.runBlocking
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import java.util.*
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
class CreateDefaultWorkspaceOnUserCreationTest {

    private val mediator: Mediator = mockk()
    private val consumer = CreateDefaultWorkspaceOnUserCreation(mediator)
    private val faker = Faker()

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `should create default workspace for new user`() = runTest {
        // Given
        val userId = UUID.randomUUID()
        val firstname = faker.name().firstName()
        val lastname = faker.name().lastName()
        val userCreatedEvent = UserCreatedEvent(
            id = userId,
            email = faker.internet().emailAddress(),
            firstName = firstname,
            lastName = lastname,
        )

        coEvery { mediator.send(any<CreateWorkspaceCommand>()) } just Runs

        // When
        consumer.consume(userCreatedEvent)

        // Then
        coVerify(exactly = 1) {
            mediator.send(
                match<CreateWorkspaceCommand> { command ->
                    command.ownerId == userId &&
                        command.name == "$firstname $lastname's Workspace" &&
                        command.description == "Default workspace created automatically upon user registration" &&
                        command.isDefault
                },
            )
        }
    }

    @Test
    fun `should create default workspace with firstname only when lastname is null`() =
        runTest {
            // Given
            val userId = UUID.randomUUID()
            val firstname = faker.name().firstName()
            val userCreatedEvent = UserCreatedEvent(
                id = userId,
                email = faker.internet().emailAddress(),
                firstName = firstname,
                lastName = null,
            )

            coEvery { mediator.send(any<CreateWorkspaceCommand>()) } just Runs

            // When
            consumer.consume(userCreatedEvent)

            // Then
            coVerify(exactly = 1) {
                mediator.send(
                    match<CreateWorkspaceCommand> { command ->
                        command.name == "$firstname's Workspace"
                    },
                )
            }
        }

    @Test
    fun `should create default workspace with lastname only when firstname is null`() =
        runTest {
            // Given
            val userId = UUID.randomUUID()
            val lastname = faker.name().lastName()
            val userCreatedEvent = UserCreatedEvent(
                id = userId,
                email = faker.internet().emailAddress(),
                firstName = null,
                lastName = lastname,
            )

            coEvery { mediator.send(any<CreateWorkspaceCommand>()) } just Runs

            // When
            consumer.consume(userCreatedEvent)

            // Then
            coVerify(exactly = 1) {
                mediator.send(
                    match<CreateWorkspaceCommand> { command ->
                        command.name == "$lastname's Workspace"
                    },
                )
            }
        }

    @Test
    fun `should create default workspace with 'My Workspace' when both names are null`() =
        runTest {
            // Given
            val userId = UUID.randomUUID()
            val userCreatedEvent = UserCreatedEvent(
                id = userId,
                email = faker.internet().emailAddress(),
                firstName = null,
                lastName = null,
            )

            coEvery { mediator.send(any<CreateWorkspaceCommand>()) } just Runs

            // When
            consumer.consume(userCreatedEvent)

            // Then
            coVerify(exactly = 1) {
                mediator.send(
                    match<CreateWorkspaceCommand> { command ->
                        command.name == "My Workspace"
                    },
                )
            }
        }

    @Test
    fun `should create default workspace with 'My Workspace' when both names contain only whitespace`() =
        runTest {
            // Given
            val userId = UUID.randomUUID()
            val firstname = "   "
            val lastname = "  \t  \n  "
            val userCreatedEvent = UserCreatedEvent(
                id = userId,
                email = faker.internet().emailAddress(),
                firstName = firstname,
                lastName = lastname,
            )

            coEvery { mediator.send(any<CreateWorkspaceCommand>()) } just Runs

            // When
            consumer.consume(userCreatedEvent)

            // Then
            coVerify(exactly = 1) {
                mediator.send(
                    match<CreateWorkspaceCommand> { command ->
                        command.name == "My Workspace"
                    },
                )
            }
        }

    @Test
    fun `should handle CommandHandlerExecutionError gracefully without throwing`() = runTest {
        // Given
        val userId = UUID.randomUUID()
        val userCreatedEvent = UserCreatedEvent(
            id = userId,
            email = faker.internet().emailAddress(),
            firstName = faker.name().firstName(),
            lastName = faker.name().lastName(),
        )

        coEvery { mediator.send(any<CreateWorkspaceCommand>()) } throws CommandHandlerExecutionError(
            "Test error",
        )

        // When & Then - should not throw exception
        assertDoesNotThrow {
            runBlocking {
                consumer.consume(userCreatedEvent)
            }
        }

        coVerify(exactly = 1) { mediator.send(any<CreateWorkspaceCommand>()) }
    }

    @Test
    fun `should handle unexpected exceptions gracefully without throwing`() = runTest {
        // Given
        val userId = UUID.randomUUID()
        val userCreatedEvent = UserCreatedEvent(
            id = userId,
            email = faker.internet().emailAddress(),
            firstName = faker.name().firstName(),
            lastName = faker.name().lastName(),
        )

        coEvery { mediator.send(any<CreateWorkspaceCommand>()) } throws RuntimeException("Unexpected error")

        // When & Then - should not throw exception
        assertDoesNotThrow {
            runBlocking {
                consumer.consume(userCreatedEvent)
            }
        }

        coVerify(exactly = 1) { mediator.send(any<CreateWorkspaceCommand>()) }
    }

    @Test
    fun `should trim whitespace from names when generating workspace name`() = runTest {
        // Given
        val userId = UUID.randomUUID()
        val firstname = "  John  "
        val lastname = "  Doe  "
        val userCreatedEvent = UserCreatedEvent(
            id = userId,
            email = faker.internet().emailAddress(),
            firstName = firstname,
            lastName = lastname,
        )

        coEvery { mediator.send(any<CreateWorkspaceCommand>()) } just Runs

        // When
        consumer.consume(userCreatedEvent)

        // Then
        coVerify(exactly = 1) {
            mediator.send(
                match<CreateWorkspaceCommand> { command ->
                    command.name == "John Doe's Workspace"
                },
            )
        }
    }
}
