package com.cvix.form.application.update

import com.cvix.UnitTest
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.form.application.SubscriberFormStub
import com.cvix.form.domain.SubscriptionFormFinderRepository
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormRepository
import com.cvix.form.domain.event.SubscriptionFormUpdatedEvent
import com.cvix.form.domain.exception.SubscriptionFormNotFoundException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.*
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach

@UnitTest
internal class UpdateSubscriberFormCommandHandlerTest {
    private val eventPublisher: EventPublisher<SubscriptionFormUpdatedEvent> =
        mockk(relaxUnitFun = true)
    private val formRepository: SubscriptionFormRepository = mockk(relaxUnitFun = true)
    private val formFinder: SubscriptionFormFinderRepository = mockk()
    private val formUpdater: SubscriberFormUpdater =
        SubscriberFormUpdater(formRepository, formFinder, eventPublisher)
    private val workspaceAuthorization: WorkspaceAuthorization = mockk(relaxUnitFun = true)
    private val commandHandler =
        UpdateSubscriberFormCommandHandler(workspaceAuthorization, formUpdater)

    private val workspaceUuid = UUID.randomUUID()
    private val userId = UUID.randomUUID()
    private val formId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        coEvery { workspaceAuthorization.ensureAccess(workspaceUuid, userId) } returns Unit
    }

    @Test
    fun `should update a form`() = runTest {
        val existingForm = SubscriberFormStub.randomForm(
            id = SubscriptionFormId(formId),
            workspaceId = WorkspaceId(workspaceUuid),
        )
        coEvery {
            formFinder.findByFormIdAndWorkspaceId(
                SubscriptionFormId(formId),
                WorkspaceId(workspaceUuid),
            )
        } returns existingForm
        coEvery { formRepository.update(any()) } answers { firstArg() }

        val command = UpdateSubscriberFormCommand(
            id = formId,
            name = "Updated Name",
            header = "Updated Header",
            description = "Updated Description",
            inputPlaceholder = "Updated Placeholder",
            buttonText = "Updated Button",
            buttonColor = "#FF0000",
            backgroundColor = "#00FF00",
            textColor = "#0000FF",
            buttonTextColor = "#FFFFFF",
            workspaceId = workspaceUuid,
            userId = userId,
        )

        commandHandler.handle(command)

        coVerify(exactly = 1) {
            formRepository.update(
                match {
                    it.id.value == formId && it.name == "Updated Name" && it.settings.header == "Updated Header"
                },
            )
        }
        coVerify(exactly = 1) { eventPublisher.publish(any<SubscriptionFormUpdatedEvent>()) }
    }

    @Test
    fun `should fail when form is not found`() = runTest {
        coEvery {
            formFinder.findByFormIdAndWorkspaceId(
                SubscriptionFormId(formId),
                WorkspaceId(workspaceUuid),
            )
        } returns null

        val command = UpdateSubscriberFormCommand(
            id = formId,
            name = "Updated Name",
            header = "Updated Header",
            description = "Updated Description",
            inputPlaceholder = "Updated Placeholder",
            buttonText = "Updated Button",
            buttonColor = "#FF0000",
            backgroundColor = "#00FF00",
            textColor = "#0000FF",
            buttonTextColor = "#FFFFFF",
            workspaceId = workspaceUuid,
            userId = userId,
        )

        assertFailsWith<SubscriptionFormNotFoundException> {
            commandHandler.handle(command)
        }

        coVerify(exactly = 0) { formRepository.update(any()) }
    }

    @Test
    fun `should not update if no changes detected`() = runTest {
        val existingForm = SubscriberFormStub.randomForm(
            id = SubscriptionFormId(formId),
            workspaceId = WorkspaceId(workspaceUuid),
        )
        coEvery {
            formFinder.findByFormIdAndWorkspaceId(
                SubscriptionFormId(formId),
                WorkspaceId(workspaceUuid),
            )
        } returns existingForm

        val command = UpdateSubscriberFormCommand(
            id = formId,
            name = existingForm.name,
            header = existingForm.settings.header,
            description = existingForm.description,
            inputPlaceholder = existingForm.settings.inputPlaceholder,
            buttonText = existingForm.settings.buttonText,
            buttonColor = existingForm.settings.buttonColor.value,
            backgroundColor = existingForm.settings.backgroundColor.value,
            textColor = existingForm.settings.textColor.value,
            buttonTextColor = existingForm.settings.buttonTextColor.value,
            workspaceId = workspaceUuid,
            userId = userId,
        )

        commandHandler.handle(command)

        coVerify(exactly = 0) { formRepository.update(any()) }
        coVerify(exactly = 0) { eventPublisher.publish(any<SubscriptionFormUpdatedEvent>()) }
    }
}
