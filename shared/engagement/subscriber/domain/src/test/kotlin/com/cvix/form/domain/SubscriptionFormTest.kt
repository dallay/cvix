package com.cvix.form.domain

import com.cvix.UnitTest
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.form.domain.event.SubscriptionFormCreatedEvent
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

@UnitTest
internal class SubscriptionFormTest {
    private val workspaceId = WorkspaceId.random()

    @Test
    fun `should create a subscription form and record created event`() {
        val id = SubscriptionFormId.random()
        val createdAt = Instant.parse("2025-01-01T10:00:00Z")
        val settings = SubscriptionFormSettings(
            settings = FormBehaviorSettings.defaults(),
            styling = FormStylingSettings(
                pageBackgroundColor = HexColor.from("ffffff"),
                backgroundColor = HexColor.from("ffffff"),
                textColor = HexColor.from("000000"),
                buttonColor = HexColor.from("00ff00"),
                buttonTextColor = HexColor.from("ffffff"),
                inputTextColor = HexColor.from("000000"),
                borderColor = HexColor.from("000000"),
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
            ),
            content = FormContentSettings(
                showHeader = true,
                showSubheader = true,
                headerTitle = "Join our newsletter",
                subheaderText = null,
                inputPlaceholder = "you@example.com",
                submitButtonText = "Subscribe",
                submittingButtonText = "Submitting...",
                showTosCheckbox = false,
                tosText = null,
                showPrivacyCheckbox = false,
                privacyText = null,
            ),
        )

        val form = SubscriptionForm.create(
            id = id,
            name = "My Form",
            description = "Description",
            settings = settings,
            workspaceId = workspaceId,
            createdBy = "tester",
            createdAt = createdAt,
        )

        assertEquals(id, form.id)
        assertEquals("My Form", form.name)
        assertEquals("Description", form.description)
        assertEquals(settings, form.settings)
        assertEquals(SubscriptionFormStatus.PUBLISHED, form.status)
        assertEquals(workspaceId, form.workspaceId)
        assertEquals(createdAt, form.createdAt)
        assertEquals("tester", form.createdBy)

        val events = form.pullDomainEvents()
        assertEquals(1, events.size)
        val event = events.first()
        assertNotNull(event)
        assertEquals(true, event is SubscriptionFormCreatedEvent)
        val createdEvent = event as SubscriptionFormCreatedEvent
        assertEquals(id, createdEvent.formId)
        assertEquals(workspaceId, createdEvent.workspaceId)
    }

    @Test
    fun `should publish an archived form and record updated event`() {
        val id = SubscriptionFormId.random()
        val settings = SubscriptionFormSettings(
            settings = FormBehaviorSettings.defaults(),
            styling = FormStylingSettings(
                pageBackgroundColor = HexColor.from("000000"),
                backgroundColor = HexColor.from("000000"),
                textColor = HexColor.from("ffffff"),
                buttonColor = HexColor.from("ff0000"),
                buttonTextColor = HexColor.from("000000"),
                inputTextColor = HexColor.from("ffffff"),
                borderColor = HexColor.from("000000"),
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
            ),
            content = FormContentSettings(
                showHeader = true,
                showSubheader = true,
                headerTitle = "Archived Header",
                subheaderText = null,
                inputPlaceholder = "email",
                submitButtonText = "Send",
                submittingButtonText = "Submitting...",
                showTosCheckbox = false,
                tosText = null,
                showPrivacyCheckbox = false,
                privacyText = null,
            ),
        )
        val form = SubscriptionForm(
            id = id,
            name = "Form",
            description = "desc",
            settings = settings,
            status = SubscriptionFormStatus.ARCHIVED,
            workspaceId = workspaceId,
            createdAt = Instant.now(),
            createdBy = "creator",
        )

        val now = Instant.parse("2025-02-02T12:00:00Z")
        val published = form.publish(updatedBy = "admin", now = now)

        assertEquals(SubscriptionFormStatus.PUBLISHED, published.status)
        assertEquals(now, published.updatedAt)
        assertEquals("admin", published.updatedBy)

        val events = published.pullDomainEvents()
        assertEquals(1, events.size)
    }

    @Test
    fun `should throw when publishing an already published form`() {
        val id = SubscriptionFormId.random()
        val form = SubscriptionForm(
            id = id,
            name = "Form",
            description = "desc",
            settings = SubscriptionFormSettings(
                settings = FormBehaviorSettings.defaults(),
                styling = FormStylingSettings(
                    pageBackgroundColor = HexColor.from("ffffff"),
                    backgroundColor = HexColor.from("ffffff"),
                    textColor = HexColor.from("000000"),
                    buttonColor = HexColor.from("abcdef"),
                    buttonTextColor = HexColor.from("ffffff"),
                    inputTextColor = HexColor.from("000000"),
                    borderColor = HexColor.from("000000"),
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
                ),
                content = FormContentSettings(
                    showHeader = true,
                    showSubheader = true,
                    headerTitle = "H",
                    subheaderText = null,
                    inputPlaceholder = "p",
                    submitButtonText = "b",
                    submittingButtonText = "Submitting...",
                    showTosCheckbox = false,
                    tosText = null,
                    showPrivacyCheckbox = false,
                    privacyText = null,
                ),
            ),
            status = SubscriptionFormStatus.PUBLISHED,
            workspaceId = workspaceId,
        )

        assertThrows(IllegalArgumentException::class.java) {
            form.publish(updatedBy = "actor")
        }
    }

    @Test
    fun `should archive a published form and record updated event`() {
        val id = SubscriptionFormId.random()
        val settings = SubscriptionFormSettings(
            settings = FormBehaviorSettings.defaults(),
            styling = FormStylingSettings(
                pageBackgroundColor = HexColor.from("ffffff"),
                backgroundColor = HexColor.from("ffffff"),
                textColor = HexColor.from("000000"),
                buttonColor = HexColor.from("abcdef"),
                buttonTextColor = HexColor.from("ffffff"),
                inputTextColor = HexColor.from("000000"),
                borderColor = HexColor.from("000000"),
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
            ),
            content = FormContentSettings(
                showHeader = true,
                showSubheader = true,
                headerTitle = "Archive",
                subheaderText = null,
                inputPlaceholder = "email",
                submitButtonText = "OK",
                submittingButtonText = "Submitting...",
                showTosCheckbox = false,
                tosText = null,
                showPrivacyCheckbox = false,
                privacyText = null,
            ),
        )
        val form = SubscriptionForm(
            id = id,
            name = "Form",
            description = "d",
            settings = settings,
            status = SubscriptionFormStatus.PUBLISHED,
            workspaceId = workspaceId,
            createdBy = "creator",
        )

        val now = Instant.parse("2025-03-03T15:00:00Z")
        val archived = form.archive(updatedBy = "archiver", now = now)

        assertEquals(SubscriptionFormStatus.ARCHIVED, archived.status)
        assertEquals(now, archived.updatedAt)
        assertEquals("archiver", archived.updatedBy)

        val events = archived.pullDomainEvents()
        assertEquals(1, events.size)
    }

    @Test
    fun `should throw when archiving an already archived form`() {
        val id = SubscriptionFormId.random()
        val form = SubscriptionForm(
            id = id,
            name = "Form",
            description = "desc",
            settings = SubscriptionFormSettings(
                settings = FormBehaviorSettings.defaults(),
                styling = FormStylingSettings(
                    pageBackgroundColor = HexColor.from("222222"),
                    backgroundColor = HexColor.from("222222"),
                    textColor = HexColor.from("333333"),
                    buttonColor = HexColor.from("111111"),
                    buttonTextColor = HexColor.from("444444"),
                    inputTextColor = HexColor.from("333333"),
                    borderColor = HexColor.from("000000"),
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
                ),
                content = FormContentSettings(
                    showHeader = true,
                    showSubheader = true,
                    headerTitle = "Hdr",
                    subheaderText = null,
                    inputPlaceholder = "ph",
                    submitButtonText = "btn",
                    submittingButtonText = "Submitting...",
                    showTosCheckbox = false,
                    tosText = null,
                    showPrivacyCheckbox = false,
                    privacyText = null,
                ),
            ),
            status = SubscriptionFormStatus.ARCHIVED,
            workspaceId = workspaceId,
        )

        assertThrows(IllegalArgumentException::class.java) {
            form.archive(updatedBy = "actor")
        }
    }

    @Test
    fun `should update details and record updated event`() {
        val id = SubscriptionFormId.random()
        val settings = SubscriptionFormSettings(
            settings = FormBehaviorSettings.defaults(),
            styling = createStylingSettings(buttonColor = "00aa00"),
            content = createContentSettings(
                headerTitle = "Old",
                inputPlaceholder = "old@ex.com",
                submitButtonText = "OldBtn",
            ),
        )
        val form = SubscriptionForm(
            id = id,
            name = "Old",
            description = "old",
            settings = settings,
            status = SubscriptionFormStatus.PUBLISHED,
            workspaceId = workspaceId,
        )

        val newSettings = SubscriptionFormSettings(
            settings = FormBehaviorSettings.defaults(),
            styling = createStylingSettings(buttonColor = "00bb00"),
            content = createContentSettings(
                headerTitle = "New",
                inputPlaceholder = "new@ex.com",
                submitButtonText = "NewBtn",
            ),
        )
        val now = Instant.parse("2025-04-04T10:10:10Z")
        val updated = form.updateDetails(
            name = "New",
            description = "new desc",
            settings = newSettings,
            updatedBy = "editor",
            now = now,
        )

        assertEquals("New", updated.name)
        assertEquals("new desc", updated.description)
        assertEquals(newSettings, updated.settings)
        assertEquals(now, updated.updatedAt)
        assertEquals("editor", updated.updatedBy)

        val events = updated.pullDomainEvents()
        assertEquals(1, events.size)
    }

    @Test
    fun `should throw when updating details with blank name`() {
        val id = SubscriptionFormId.random()
        val form = SubscriptionForm(
            id = id,
            name = "Name",
            description = "desc",
            settings = SubscriptionFormSettings(
                settings = FormBehaviorSettings.defaults(),
                styling = FormStylingSettings(
                    pageBackgroundColor = HexColor.from("ffffff"),
                    backgroundColor = HexColor.from("ffffff"),
                    textColor = HexColor.from("000000"),
                    buttonColor = HexColor.from("abcdef"),
                    buttonTextColor = HexColor.from("ffffff"),
                    inputTextColor = HexColor.from("000000"),
                    borderColor = HexColor.from("000000"),
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
                ),
                content = FormContentSettings(
                    showHeader = true,
                    showSubheader = true,
                    headerTitle = "A",
                    subheaderText = null,
                    inputPlaceholder = "a",
                    submitButtonText = "b",
                    submittingButtonText = "Submitting...",
                    showTosCheckbox = false,
                    tosText = null,
                    showPrivacyCheckbox = false,
                    privacyText = null,
                ),
            ),
            status = SubscriptionFormStatus.PUBLISHED,
            workspaceId = workspaceId,
        )

        assertThrows(IllegalArgumentException::class.java) {
            form.updateDetails(
                name = "",
                description = "d",
                settings = form.settings,
                updatedBy = "u",
            )
        }
    }

    // Helper methods to reduce test verbosity
    private fun createStylingSettings(buttonColor: String = "00ff00"): FormStylingSettings =
        FormStylingSettings(
            pageBackgroundColor = HexColor.from("ffffff"),
            backgroundColor = HexColor.from("ffffff"),
            textColor = HexColor.from("000000"),
            buttonColor = HexColor.from(buttonColor),
            buttonTextColor = HexColor.from("ffffff"),
            inputTextColor = HexColor.from("000000"),
            borderColor = HexColor.from("000000"),
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
        )

    private fun createContentSettings(
        headerTitle: String = "Test",
        inputPlaceholder: String = "test@ex.com",
        submitButtonText: String = "TestBtn"
    ): FormContentSettings =
        FormContentSettings(
            showHeader = true,
            showSubheader = true,
            headerTitle = headerTitle,
            subheaderText = null,
            inputPlaceholder = inputPlaceholder,
            submitButtonText = submitButtonText,
            submittingButtonText = "Submitting...",
            showTosCheckbox = false,
            tosText = null,
            showPrivacyCheckbox = false,
            privacyText = null,
        )
}
