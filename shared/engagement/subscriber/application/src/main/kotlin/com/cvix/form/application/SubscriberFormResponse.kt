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
 * @property status Current status of the subscription form.
 * @property confirmationRequired Whether the subscriber needs to confirm their email.
 * @property workspaceId The workspace this form belongs to.
 * @property createdAt Timestamp when the form was created (ISO-8601).
 * @property updatedAt Timestamp when the form was last updated (ISO-8601), or null if never updated.
 * @property successActionType Action type on success (SHOW_MESSAGE or REDIRECT).
 * @property successMessage Success message when successActionType is SHOW_MESSAGE.
 * @property redirectUrl Redirect URL when successActionType is REDIRECT.
 * @property borderColor Border color hex code.
 * @property borderStyle Border style (solid, dashed, dotted).
 * @property shadow Box shadow style.
 * @property borderThickness Border thickness in pixels.
 * @property width Form width.
 * @property height Form height.
 * @property horizontalAlignment Horizontal alignment (left, center, right).
 * @property verticalAlignment Vertical alignment (top, center, bottom).
 * @property padding Internal padding in pixels.
 * @property gap Gap between elements in pixels.
 * @property cornerRadius Border radius in pixels.
 * @property showHeader Whether to show the header.
 * @property showSubheader Whether to show the subheader.
 * @property headerTitle Header title text.
 * @property subheaderText Subheader text.
 * @property submittingButtonText Submit button text during submission.
 * @property showTosCheckbox Whether to show Terms of Service checkbox.
 * @property tosText Terms of Service checkbox label text.
 * @property showPrivacyCheckbox Whether to show Privacy Policy checkbox.
 * @property privacyText Privacy Policy checkbox label text.
 * @property pageBackgroundColor Page background hex color.
 * @property inputTextColor Input text hex color.
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

    @field:Schema(description = "Page background hex color for iframe body", example = "#ffffff")
    val pageBackgroundColor: String,

    @field:Schema(description = "Form background hex color", example = "#ffffff")
    val backgroundColor: String,

    @field:Schema(description = "Main text hex color", example = "#000000")
    val textColor: String,

    @field:Schema(description = "Button text hex color", example = "#ffffff")
    val buttonTextColor: String,

    @field:Schema(description = "Input text hex color", example = "#000000")
    val inputTextColor: String,

    @field:Schema(description = "Current status of the subscription form", example = "PUBLISHED")
    val status: String,

    @field:Schema(description = "Whether confirmation is required", example = "true")
    val confirmationRequired: Boolean,

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

    // Behavior settings
    @field:Schema(
        description = "Action type on success",
        example = "SHOW_MESSAGE",
        allowableValues = ["SHOW_MESSAGE", "REDIRECT"],
    )
    val successActionType: String,

    @field:Schema(
        description = "Success message when successActionType is SHOW_MESSAGE",
        example = "Success! Check your email.",
    )
    val successMessage: String?,

    @field:Schema(
        description = "Redirect URL when successActionType is REDIRECT",
        example = "https://example.com/thank-you",
    )
    val redirectUrl: String?,

    // Styling settings - new fields
    @field:Schema(description = "Border color (hex)", example = "#000000")
    val borderColor: String,

    @field:Schema(description = "Border style", example = "solid")
    val borderStyle: String,

    @field:Schema(description = "Box shadow style", example = "none")
    val shadow: String,

    @field:Schema(description = "Border thickness in pixels", example = "0")
    val borderThickness: Int,

    @field:Schema(description = "Form width", example = "auto")
    val width: String,

    @field:Schema(description = "Form height", example = "auto")
    val height: String,

    @field:Schema(description = "Horizontal alignment", example = "center")
    val horizontalAlignment: String,

    @field:Schema(description = "Vertical alignment", example = "center")
    val verticalAlignment: String,

    @field:Schema(description = "Internal padding in pixels", example = "16")
    val padding: Int,

    @field:Schema(description = "Gap between elements in pixels", example = "16")
    val gap: Int,

    @field:Schema(description = "Border radius in pixels", example = "8")
    val cornerRadius: Int,

    // Content settings
    @field:Schema(description = "Whether to show the header", example = "true")
    val showHeader: Boolean,

    @field:Schema(description = "Whether to show the subheader", example = "true")
    val showSubheader: Boolean,

    @field:Schema(description = "Header title text", example = "Join our newsletter")
    val headerTitle: String,

    @field:Schema(description = "Subheader text", example = "Get the latest updates")
    val subheaderText: String?,

    @field:Schema(description = "Submit button text during submission", example = "Submitting...")
    val submittingButtonText: String,

    @field:Schema(description = "Whether to show Terms of Service checkbox", example = "false")
    val showTosCheckbox: Boolean,

    @field:Schema(description = "Terms of Service text", example = "I agree to the Terms of Service")
    val tosText: String?,

    @field:Schema(description = "Whether to show Privacy Policy checkbox", example = "false")
    val showPrivacyCheckbox: Boolean,

    @field:Schema(description = "Privacy Policy text", example = "I agree to the Privacy Policy")
    val privacyText: String?,
) : Response {
    companion object {
        /**
         * Creates a new SubscriberFormResponse from a given SubscriptionForm.
         */
        fun from(form: SubscriptionForm) = SubscriberFormResponse(
            id = form.id.value.toString(),
            name = form.name,
            header = form.settings.content.headerTitle,
            description = form.description,
            inputPlaceholder = form.settings.content.inputPlaceholder,
            buttonText = form.settings.content.submitButtonText,
            buttonColor = form.settings.styling.buttonColor.value,
            pageBackgroundColor = form.settings.styling.pageBackgroundColor.value,
            backgroundColor = form.settings.styling.backgroundColor.value,
            textColor = form.settings.styling.textColor.value,
            buttonTextColor = form.settings.styling.buttonTextColor.value,
            inputTextColor = form.settings.styling.inputTextColor.value,
            status = form.status.name,
            confirmationRequired = form.settings.settings.confirmationRequired,
            workspaceId = form.workspaceId.value.toString(),
            createdAt = form.createdAt.toString(),
            updatedAt = form.updatedAt?.toString(),
            // Behavior settings
            successActionType = form.settings.settings.successActionType.name,
            successMessage = form.settings.settings.successMessage,
            redirectUrl = form.settings.settings.redirectUrl,
            // Styling settings
            borderColor = form.settings.styling.borderColor.value,
            borderStyle = form.settings.styling.borderStyle,
            shadow = form.settings.styling.shadow,
            borderThickness = form.settings.styling.borderThickness,
            width = form.settings.styling.width,
            height = form.settings.styling.height,
            horizontalAlignment = form.settings.styling.horizontalAlignment,
            verticalAlignment = form.settings.styling.verticalAlignment,
            padding = form.settings.styling.padding,
            gap = form.settings.styling.gap,
            cornerRadius = form.settings.styling.cornerRadius,
            // Content settings
            showHeader = form.settings.content.showHeader,
            showSubheader = form.settings.content.showSubheader,
            headerTitle = form.settings.content.headerTitle,
            subheaderText = form.settings.content.subheaderText,
            submittingButtonText = form.settings.content.submittingButtonText,
            showTosCheckbox = form.settings.content.showTosCheckbox,
            tosText = form.settings.content.tosText,
            showPrivacyCheckbox = form.settings.content.showPrivacyCheckbox,
            privacyText = form.settings.content.privacyText,
        )
    }
}
