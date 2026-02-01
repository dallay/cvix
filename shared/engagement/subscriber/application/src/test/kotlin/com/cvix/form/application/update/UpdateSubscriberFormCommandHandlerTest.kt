package com.cvix.form.application.update

import com.cvix.UnitTest
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.outbox.OutboxRepository
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.form.application.SubscriberFormStub
import com.cvix.form.domain.SubscriptionFormFinderRepository
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormRepository
import com.cvix.form.domain.exception.SubscriptionFormNotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
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
    private val outboxRepository: OutboxRepository = mockk(relaxUnitFun = true)
    private val objectMapper: ObjectMapper = mockk(relaxed = true)
    private val formRepository: SubscriptionFormRepository = mockk(relaxUnitFun = true)
    private val formFinder: SubscriptionFormFinderRepository = mockk()
    private val formUpdater: SubscriberFormUpdater =
        SubscriberFormUpdater(formRepository, formFinder, outboxRepository, objectMapper)
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

    private fun createValidCommand(
        id: UUID = formId,
        workspaceId: UUID = workspaceUuid,
        userId: UUID = this.userId,
        name: String = "Updated Name",
        headerTitle: String = "Updated Header",
    ): UpdateSubscriberFormCommand {
        return UpdateSubscriberFormCommand(
            id = id,
            name = name,
            description = "Updated Description",
            confirmationRequired = true,
            successActionType = "SHOW_MESSAGE",
            successMessage = "Success!",
            redirectUrl = null,
            // Styling
            buttonColor = "#FF0000",
            pageBackgroundColor = "#FFFFFF",
            backgroundColor = "#00FF00",
            textColor = "#0000FF",
            buttonTextColor = "#FFFFFF",
            inputTextColor = "#000000",
            borderColor = "#000000",
            borderStyle = "solid",
            shadow = "none",
            borderThickness = 0,
            width = "auto",
            height = "auto",
            horizontalAlignment = "center",
            verticalAlignment = "center",
            padding = 16,
            gap = 16,
            cornerRadius = 8,
            // Content
            showHeader = true,
            showSubheader = true,
            headerTitle = headerTitle,
            subheaderText = "Updated subheader",
            inputPlaceholder = "Updated Placeholder",
            submitButtonText = "Updated Button",
            submittingButtonText = "Submitting...",
            showTosCheckbox = false,
            tosText = null,
            showPrivacyCheckbox = false,
            privacyText = null,
            workspaceId = workspaceId,
            userId = userId,
        )
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

        val command = createValidCommand()

        commandHandler.handle(command)

        coVerify(exactly = 1) {
            formRepository.update(
                match {
                    it.id.value == formId &&
                        it.name == "Updated Name" &&
                        it.settings.content.headerTitle == "Updated Header"
                },
            )
        }
        coVerify(exactly = 1) { outboxRepository.save(any()) }
    }

    @Test
    fun `should fail when form is not found`() = runTest {
        coEvery {
            formFinder.findByFormIdAndWorkspaceId(
                SubscriptionFormId(formId),
                WorkspaceId(workspaceUuid),
            )
        } returns null

        val command = createValidCommand()

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

        val command = createValidCommand(
            name = existingForm.name,
            headerTitle = existingForm.settings.content.headerTitle,
        ).copy(
            description = existingForm.description,
            confirmationRequired = existingForm.settings.settings.confirmationRequired,
            successActionType = existingForm.settings.settings.successActionType.name,
            successMessage = existingForm.settings.settings.successMessage,
            redirectUrl = existingForm.settings.settings.redirectUrl,
            // Styling
            buttonColor = existingForm.settings.styling.buttonColor.value,
            pageBackgroundColor = existingForm.settings.styling.pageBackgroundColor.value,
            backgroundColor = existingForm.settings.styling.backgroundColor.value,
            textColor = existingForm.settings.styling.textColor.value,
            buttonTextColor = existingForm.settings.styling.buttonTextColor.value,
            inputTextColor = existingForm.settings.styling.inputTextColor.value,
            borderColor = existingForm.settings.styling.borderColor.value,
            borderStyle = existingForm.settings.styling.borderStyle,
            shadow = existingForm.settings.styling.shadow,
            borderThickness = existingForm.settings.styling.borderThickness,
            width = existingForm.settings.styling.width,
            height = existingForm.settings.styling.height,
            horizontalAlignment = existingForm.settings.styling.horizontalAlignment,
            verticalAlignment = existingForm.settings.styling.verticalAlignment,
            padding = existingForm.settings.styling.padding,
            gap = existingForm.settings.styling.gap,
            cornerRadius = existingForm.settings.styling.cornerRadius,
            // Content
            showHeader = existingForm.settings.content.showHeader,
            showSubheader = existingForm.settings.content.showSubheader,
            subheaderText = existingForm.settings.content.subheaderText,
            inputPlaceholder = existingForm.settings.content.inputPlaceholder,
            submitButtonText = existingForm.settings.content.submitButtonText,
            submittingButtonText = existingForm.settings.content.submittingButtonText,
            showTosCheckbox = existingForm.settings.content.showTosCheckbox,
            tosText = existingForm.settings.content.tosText,
            showPrivacyCheckbox = existingForm.settings.content.showPrivacyCheckbox,
            privacyText = existingForm.settings.content.privacyText,
        )

        commandHandler.handle(command)

        coVerify(exactly = 0) { formRepository.update(any()) }
        coVerify(exactly = 0) { outboxRepository.save(any()) }
    }
}
