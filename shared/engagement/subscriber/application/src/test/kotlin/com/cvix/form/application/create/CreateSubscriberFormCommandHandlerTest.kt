package com.cvix.form.application.create

import com.cvix.UnitTest
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.form.application.SubscriberFormStub
import com.cvix.form.domain.SubscriptionFormRepository
import com.cvix.form.domain.event.SubscriptionFormCreatedEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach

@UnitTest
internal class CreateSubscriberFormCommandHandlerTest {
    private val eventPublisher: EventPublisher<SubscriptionFormCreatedEvent> =
        mockk(relaxUnitFun = true)
    private val formRepository: SubscriptionFormRepository = mockk(relaxUnitFun = true)
    private val formCreator: SubscriberFormCreator =
        SubscriberFormCreator(formRepository, eventPublisher)
    private val workspaceAuthorization: WorkspaceAuthorization = mockk(relaxUnitFun = true)
    private val commandHandler = CreateSubscriberFormCommandHandler(
        workspaceAuthorization,
        formCreator,
    )
    private lateinit var settings: com.cvix.form.domain.SubscriptionFormSettings
    private lateinit var workspaceUuid: UUID
    private val userId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        settings = SubscriberFormStub.randomSettings()
        workspaceUuid = WorkspaceId.random().value

        // By default, return the same entity passed to repository.create so tests can assert IDs
        coEvery { formRepository.create(any()) } answers { firstArg() }

        coEvery { eventPublisher.publish(any<SubscriptionFormCreatedEvent>()) } returns Unit
        coEvery {
            workspaceAuthorization.ensureAccess(workspaceUuid, userId)
        } returns Unit
    }

    @Test
    fun `should create a form`() = runTest {
        // Arrange
        val commandId = UUID.randomUUID()
        val command = CreateSubscriberFormCommand(
            id = commandId,
            name = "My Form",
            header = settings.header,
            description = "A description",
            inputPlaceholder = settings.inputPlaceholder,
            buttonText = settings.buttonText,
            buttonColor = settings.buttonColor.value,
            backgroundColor = settings.backgroundColor.value,
            textColor = settings.textColor.value,
            buttonTextColor = settings.buttonTextColor.value,
            workspaceId = workspaceUuid,
            userId = userId,
        )

        // Act
        commandHandler.handle(command)

        // Assert - repository create called and event published
        coVerify(exactly = 1) { formRepository.create(match { it.id.value == commandId }) }
        coVerify(exactly = 1) { eventPublisher.publish(any<SubscriptionFormCreatedEvent>()) }
    }

    @Test
    fun `should fail when user is not a workspace member`() = runTest {
        // Arrange: make workspaceAuthorization throw when ensuring access
        val otherUser = UUID.randomUUID()
        coEvery {
            workspaceAuthorization.ensureAccess(workspaceUuid, otherUser)
        } throws IllegalStateException("access denied")

        val command = CreateSubscriberFormCommand(
            id = UUID.randomUUID(),
            name = "Name",
            header = settings.header,
            description = settings.header,
            inputPlaceholder = settings.inputPlaceholder,
            buttonText = settings.buttonText,
            buttonColor = settings.buttonColor.value,
            backgroundColor = settings.backgroundColor.value,
            textColor = settings.textColor.value,
            buttonTextColor = settings.buttonTextColor.value,
            workspaceId = workspaceUuid,
            userId = otherUser,
        )

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            commandHandler.handle(command)
        }

        // ensure repository create was not called
        coVerify(exactly = 0) { formRepository.create(any()) }
        coVerify(exactly = 0) { eventPublisher.publish(any<SubscriptionFormCreatedEvent>()) }
    }

    @Test
    fun `should fail when buttonColor is not a valid hex color`(): Unit = runTest {
        val command = CreateSubscriberFormCommand(
            id = UUID.randomUUID(),
            name = "Name",
            header = settings.header,
            description = settings.header,
            inputPlaceholder = settings.inputPlaceholder,
            buttonText = settings.buttonText,
            buttonColor = "not-a-hex",
            backgroundColor = settings.backgroundColor.value,
            textColor = settings.textColor.value,
            buttonTextColor = settings.buttonTextColor.value,
            workspaceId = workspaceUuid,
            userId = userId,
        )

        val error = assertFailsWith<IllegalArgumentException> {
            commandHandler.handle(command)
        }
        assertEquals("Invalid hexadecimal color code: not-a-hex", error.message)

        coVerify(exactly = 0) { formRepository.create(any()) }
        coVerify(exactly = 0) { eventPublisher.publish(any<SubscriptionFormCreatedEvent>()) }
    }

    @Test
    fun `should fail when repository reports duplicate form id`() = runTest {
        // Arrange: repository throws when creating entity with an existing id
        val duplicateId = UUID.randomUUID()
        coEvery { formRepository.create(match { it.id.value == duplicateId }) } throws IllegalStateException(
            "duplicate id",
        )

        val command = CreateSubscriberFormCommand(
            id = duplicateId,
            name = "Duplicated",
            header = settings.header,
            description = "desc",
            inputPlaceholder = settings.inputPlaceholder,
            buttonText = settings.buttonText,
            buttonColor = settings.buttonColor.value,
            backgroundColor = settings.backgroundColor.value,
            textColor = settings.textColor.value,
            buttonTextColor = settings.buttonTextColor.value,
            workspaceId = workspaceUuid,
            userId = userId,
        )

        // Act & Assert
        val error = assertFailsWith<IllegalStateException> {
            commandHandler.handle(command)
        }
        assertEquals("duplicate id", error.message)

        coVerify(exactly = 1) { formRepository.create(match { it.id.value == duplicateId }) }
        coVerify(exactly = 0) { eventPublisher.publish(any<SubscriptionFormCreatedEvent>()) }
    }

    @Test
    fun `should not publish event when repository fails`(): Unit = runTest {
        // Arrange: repository fails with runtime exception
        val failingId = UUID.randomUUID()
        coEvery { formRepository.create(any()) } throws RuntimeException("db down")

        val command = CreateSubscriberFormCommand(
            id = failingId,
            name = "WillFail",
            header = settings.header,
            description = settings.header,
            inputPlaceholder = settings.inputPlaceholder,
            buttonText = settings.buttonText,
            buttonColor = settings.buttonColor.value,
            backgroundColor = settings.backgroundColor.value,
            textColor = settings.textColor.value,
            buttonTextColor = settings.buttonTextColor.value,
            workspaceId = workspaceUuid,
            userId = userId,
        )

        // Act & Assert
        assertFailsWith<RuntimeException> {
            commandHandler.handle(command)
        }

        coVerify(exactly = 1) { formRepository.create(any()) }
        coVerify(exactly = 0) { eventPublisher.publish(any<SubscriptionFormCreatedEvent>()) }
    }

    @Test
    fun `should validate max lengths for name and header`() = runTest {
        val longString = "a".repeat(121)
        val command = CreateSubscriberFormCommand(
            id = UUID.randomUUID(),
            name = longString,
            header = settings.header,
            description = "desc",
            inputPlaceholder = settings.inputPlaceholder,
            buttonText = settings.buttonText,
            buttonColor = settings.buttonColor.value,
            backgroundColor = settings.backgroundColor.value,
            textColor = settings.textColor.value,
            buttonTextColor = settings.buttonTextColor.value,
            workspaceId = workspaceUuid,
            userId = userId,
        )

        val error = assertFailsWith<IllegalArgumentException> {
            commandHandler.handle(command)
        }
        assertEquals("Form name must be at most 120 characters", error.message)

        // header too long
        val command2 = CreateSubscriberFormCommand(
            id = UUID.randomUUID(),
            name = "Name",
            header = "b".repeat(121),
            description = "desc",
            inputPlaceholder = settings.inputPlaceholder,
            buttonText = settings.buttonText,
            buttonColor = settings.buttonColor.value,
            backgroundColor = settings.backgroundColor.value,
            textColor = settings.textColor.value,
            buttonTextColor = settings.buttonTextColor.value,
            workspaceId = workspaceUuid,
            userId = userId,
        )

        val error2 = assertFailsWith<IllegalArgumentException> {
            commandHandler.handle(command2)
        }
        assertEquals("Form header must be at most 120 characters", error2.message)
    }

    @Test
    fun `should trim name and header before creation`() = runTest {
        val commandId = UUID.randomUUID()
        val command = CreateSubscriberFormCommand(
            id = commandId,
            name = "  My Trimmed Name  ",
            header = "  Header text  ",
            description = "desc",
            inputPlaceholder = settings.inputPlaceholder,
            buttonText = settings.buttonText,
            buttonColor = settings.buttonColor.value,
            backgroundColor = settings.backgroundColor.value,
            textColor = settings.textColor.value,
            buttonTextColor = settings.buttonTextColor.value,
            workspaceId = workspaceUuid,
            userId = userId,
        )

        commandHandler.handle(command)

        coVerify(exactly = 1) {
            formRepository.create(
                match {
                    it.name == "My Trimmed Name" && it.settings.header == "Header text" && it.id.value == commandId
                },
            )
        }
        coVerify(exactly = 1) { eventPublisher.publish(any<SubscriptionFormCreatedEvent>()) }
    }

    @Test
    fun `should reject blank name`() = runTest {
        val command = CreateSubscriberFormCommand(
            id = UUID.randomUUID(),
            name = "   ",
            header = settings.header,
            description = "desc",
            inputPlaceholder = settings.inputPlaceholder,
            buttonText = settings.buttonText,
            buttonColor = settings.buttonColor.value,
            backgroundColor = settings.backgroundColor.value,
            textColor = settings.textColor.value,
            buttonTextColor = settings.buttonTextColor.value,
            workspaceId = workspaceUuid,
            userId = userId,
        )
        val error = assertFailsWith<IllegalArgumentException> {
            commandHandler.handle(command)
        }
        assertEquals("Form name must not be blank", error.message)
    }

    @Test
    fun `should reject blank header`() = runTest {
        val command = CreateSubscriberFormCommand(
            id = UUID.randomUUID(),
            name = "Valid Name",
            header = "   ",
            description = "desc",
            inputPlaceholder = settings.inputPlaceholder,
            buttonText = settings.buttonText,
            buttonColor = settings.buttonColor.value,
            backgroundColor = settings.backgroundColor.value,
            textColor = settings.textColor.value,
            buttonTextColor = settings.buttonTextColor.value,
            workspaceId = workspaceUuid,
            userId = userId,
        )
        val error = assertFailsWith<IllegalArgumentException> {
            commandHandler.handle(command)
        }
        assertEquals("Form header must not be blank", error.message)
    }

    @Test
    fun `should allow name and header at max length`() = runTest {
        val maxLen = 120
        val commandId = UUID.randomUUID()
        val command = CreateSubscriberFormCommand(
            id = commandId,
            name = "n".repeat(maxLen),
            header = "h".repeat(maxLen),
            description = "desc",
            inputPlaceholder = settings.inputPlaceholder,
            buttonText = settings.buttonText,
            buttonColor = settings.buttonColor.value,
            backgroundColor = settings.backgroundColor.value,
            textColor = settings.textColor.value,
            buttonTextColor = settings.buttonTextColor.value,
            workspaceId = workspaceUuid,
            userId = userId,
        )
        commandHandler.handle(command)
        coVerify(exactly = 1) {
            formRepository.create(
                match {
                    it.name == "n".repeat(maxLen) && it.settings.header == "h".repeat(maxLen) &&
                        it.id.value == commandId
                },
            )
        }
        coVerify(exactly = 1) { eventPublisher.publish(any<SubscriptionFormCreatedEvent>()) }
    }

    @Test
    fun `should propagate exception from workspaceAuthorization`() = runTest {
        coEvery {
            workspaceAuthorization.ensureAccess(
                workspaceUuid,
                userId,
            )
        } throws IllegalStateException("forbidden")
        val command = CreateSubscriberFormCommand(
            id = UUID.randomUUID(),
            name = "Valid Name",
            header = settings.header,
            description = "desc",
            inputPlaceholder = settings.inputPlaceholder,
            buttonText = settings.buttonText,
            buttonColor = settings.buttonColor.value,
            backgroundColor = settings.backgroundColor.value,
            textColor = settings.textColor.value,
            buttonTextColor = settings.buttonTextColor.value,
            workspaceId = workspaceUuid,
            userId = userId,
        )
        val error = assertFailsWith<IllegalStateException> {
            commandHandler.handle(command)
        }
        assertEquals("forbidden", error.message)
        coVerify(exactly = 0) { formRepository.create(any()) }
        coVerify(exactly = 0) { eventPublisher.publish(any<SubscriptionFormCreatedEvent>()) }
    }

    @Test
    fun `should allow whitespace-only description`() = runTest {
        val commandId = UUID.randomUUID()
        val command = CreateSubscriberFormCommand(
            id = commandId,
            name = "Valid Name",
            header = settings.header,
            description = "   ",
            inputPlaceholder = settings.inputPlaceholder,
            buttonText = settings.buttonText,
            buttonColor = settings.buttonColor.value,
            backgroundColor = settings.backgroundColor.value,
            textColor = settings.textColor.value,
            buttonTextColor = settings.buttonTextColor.value,
            workspaceId = workspaceUuid,
            userId = userId,
        )
        commandHandler.handle(command)
        coVerify(exactly = 1) { formRepository.create(match { it.id.value == commandId }) }
        coVerify(exactly = 1) { eventPublisher.publish(any<SubscriptionFormCreatedEvent>()) }
    }
}
