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

    private fun createValidCommand(
        id: UUID = UUID.randomUUID(),
        name: String = "My Form",
        workspaceId: UUID = workspaceUuid,
        userId: UUID = this.userId,
    ): CreateSubscriberFormCommand = CreateSubscriberFormCommand(
        id = id,
        name = name,
        description = "A description",
        confirmationRequired = settings.settings.confirmationRequired,
        successActionType = settings.settings.successActionType.name,
        successMessage = settings.settings.successMessage,
        redirectUrl = settings.settings.redirectUrl,
        styling = StylingInput(
            buttonColor = settings.styling.buttonColor.value,
            pageBackgroundColor = settings.styling.pageBackgroundColor.value,
            backgroundColor = settings.styling.backgroundColor.value,
            textColor = settings.styling.textColor.value,
            buttonTextColor = settings.styling.buttonTextColor.value,
            inputTextColor = settings.styling.inputTextColor.value,
            borderColor = settings.styling.borderColor.value,
            borderStyle = settings.styling.borderStyle,
            shadow = settings.styling.shadow,
            borderThickness = settings.styling.borderThickness,
            width = settings.styling.width,
            height = settings.styling.height,
            horizontalAlignment = settings.styling.horizontalAlignment,
            verticalAlignment = settings.styling.verticalAlignment,
            padding = settings.styling.padding,
            gap = settings.styling.gap,
            cornerRadius = settings.styling.cornerRadius,
        ),
        content = ContentInput(
            showHeader = settings.content.showHeader,
            showSubheader = settings.content.showSubheader,
            headerTitle = settings.content.headerTitle,
            subheaderText = settings.content.subheaderText,
            inputPlaceholder = settings.content.inputPlaceholder,
            submitButtonText = settings.content.submitButtonText,
            submittingButtonText = settings.content.submittingButtonText,
            showTosCheckbox = settings.content.showTosCheckbox,
            tosText = settings.content.tosText,
            showPrivacyCheckbox = settings.content.showPrivacyCheckbox,
            privacyText = settings.content.privacyText,
        ),
        workspaceId = workspaceId,
        userId = userId,
    )

    @Test
    fun `should create a form`() = runTest {
        // Arrange
        val commandId = UUID.randomUUID()
        val command = createValidCommand(id = commandId)

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

        val command = createValidCommand(userId = otherUser)

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
        val command = createValidCommand().let {
            it.copy(styling = it.styling.copy(buttonColor = "not-a-hex"))
        }

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

        val command = createValidCommand(id = duplicateId)

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

        val command = createValidCommand(id = failingId)

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
        val command = createValidCommand(name = longString)

        val error = assertFailsWith<IllegalArgumentException> {
            commandHandler.handle(command)
        }
        assertEquals("Form name must be at most 120 characters", error.message)

        // header too long
        val command2 = createValidCommand().let {
            it.copy(content = it.content.copy(headerTitle = "b".repeat(121)))
        }

        val error2 = assertFailsWith<IllegalArgumentException> {
            commandHandler.handle(command2)
        }
        assertEquals("Form header must be at most 120 characters", error2.message)
    }

    @Test
    fun `should trim name and header before creation`() = runTest {
        val commandId = UUID.randomUUID()
        val baseCommand = createValidCommand(id = commandId)
        val command = baseCommand.copy(
            name = "  My Trimmed Name  ",
            content = baseCommand.content.copy(headerTitle = "  Header text  "),
        )

        commandHandler.handle(command)

        coVerify(exactly = 1) {
            formRepository.create(
                match {
                    it.name == "My Trimmed Name" &&
                        it.settings.content.headerTitle == "Header text" &&
                        it.id.value == commandId
                },
            )
        }
        coVerify(exactly = 1) { eventPublisher.publish(any<SubscriptionFormCreatedEvent>()) }
    }

    @Test
    fun `should reject blank name`() = runTest {
        val command = createValidCommand().copy(name = "   ")
        val error = assertFailsWith<IllegalArgumentException> {
            commandHandler.handle(command)
        }
        assertEquals("Form name must not be blank", error.message)
    }

    @Test
    fun `should reject blank header`() = runTest {
        val command = createValidCommand().let {
            it.copy(content = it.content.copy(headerTitle = "   "))
        }
        val error = assertFailsWith<IllegalArgumentException> {
            commandHandler.handle(command)
        }
        assertEquals("Form header must not be blank", error.message)
    }

    @Test
    fun `should allow name and header at max length`() = runTest {
        val maxLen = 120
        val commandId = UUID.randomUUID()
        val baseCommand = createValidCommand(id = commandId)
        val command = baseCommand.copy(
            name = "n".repeat(maxLen),
            content = baseCommand.content.copy(headerTitle = "h".repeat(maxLen)),
        )
        commandHandler.handle(command)
        coVerify(exactly = 1) {
            formRepository.create(
                match {
                    it.name == "n".repeat(maxLen) &&
                        it.settings.content.headerTitle == "h".repeat(maxLen) &&
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
        val command = createValidCommand()
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
        val command = createValidCommand(id = commandId).copy(description = "   ")
        commandHandler.handle(command)
        coVerify(exactly = 1) { formRepository.create(match { it.id.value == commandId }) }
        coVerify(exactly = 1) { eventPublisher.publish(any<SubscriptionFormCreatedEvent>()) }
    }
}
