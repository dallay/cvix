package com.cvix.form.application

import com.cvix.common.domain.bus.query.Response
import com.cvix.form.domain.SubscriptionForm
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Data class representing a response for a subscription form details.
 *
 * This DTO is used to return the configuration and metadata of a subscription form
 * to the frontend or other consuming services.
 *
 * @property id Unique identifier of the subscription form.
 * @property name Internal name of the form for management purposes.
 * @property header Public-facing header text displayed on the form.
 * @property description Detailed description or call to action for the form.
 * @property inputPlaceholder Placeholder text for the email input field.
 * @property buttonText Text displayed on the submission button.
 * @property buttonColor Hex color code for the submission button.
 * @property backgroundColor Hex color code for the form background.
 * @property textColor Hex color code for the general form text.
 * @property buttonTextColor Hex color code for the text on the submission button.
 * @property workspaceId The workspace this form belongs to.
 * @property createdAt Timestamp when the form was created (ISO-8601).
 * @property updatedAt Timestamp when the form was last updated (ISO-8601), or null if never updated.
 */
data class SubscriberFormResponse(
    @field:Schema(
        description =
        "Unique identifier of the subscription form",
        example = "550e8400-e29b-41d4-a716-446655440000",
    )
    val id: String,

    @field:Schema(description = "Internal name of the form", example = "Newsletter Signup")
    val name: String,

    @field:Schema(description = "Public-facing header text", example = "Join our newsletter")
    val header: String,

    @field:Schema(
        description =
        "Detailed description or call to action",
        example = "Get the latest updates directly in your inbox",
    )
    val description: String,

    @field:Schema(description = "Placeholder text for the email input", example = "your@email.com")
    val inputPlaceholder: String,

    @field:Schema(description = "Text displayed on the button", example = "Subscribe")
    val buttonText: String,

    @field:Schema(description = "Button background hex color", example = "#000000")
    val buttonColor: String,

    @field:Schema(description = "Form background hex color", example = "#ffffff")
    val backgroundColor: String,

    @field:Schema(description = "Main text hex color", example = "#000000")
    val textColor: String,

    @field:Schema(description = "Button text hex color", example = "#ffffff")
    val buttonTextColor: String,

    @field:Schema(
        description = "Workspace identifier",
        example = "770e8400-e29b-41d4-a716-446655441111",
    )
    val workspaceId: String,

    @field:Schema(description = "Creation timestamp", example = "2025-01-26T10:00:00Z")
    val createdAt: String,

    @field:Schema(
        description = "Last update timestamp",
        example = "2025-01-26T12:00:00Z",
        nullable = true,
    )
    val updatedAt: String?,
) : Response {
    companion object {
        /**
         * Creates a new SubscriberFormResponse from a given SubscriptionForm.
         */
        fun from(form: SubscriptionForm) = SubscriberFormResponse(
            id = form.id.value.toString(),
            name = form.name,
            header = form.settings.header,
            description = form.description,
            inputPlaceholder = form.settings.inputPlaceholder,
            buttonText = form.settings.buttonText,
            buttonColor = form.settings.buttonColor.value,
            backgroundColor = form.settings.backgroundColor.value,
            textColor = form.settings.textColor.value,
            buttonTextColor = form.settings.buttonTextColor.value,
            workspaceId = form.workspaceId.value.toString(),
            createdAt = form.createdAt.toString(),
            updatedAt = form.updatedAt?.toString(),
        )
    }
}
