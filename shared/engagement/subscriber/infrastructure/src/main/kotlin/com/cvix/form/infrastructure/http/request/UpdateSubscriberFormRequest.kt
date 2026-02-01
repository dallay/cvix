package com.cvix.form.infrastructure.http.request

import com.cvix.form.application.update.UpdateSubscriberFormCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.*

/**
 * Request to update a subscription form
 */
@Schema(description = "Request to update a subscription form")
data class UpdateSubscriberFormRequest(
    @field:Schema(description = "Form name", example = "Newsletter Signup", required = true)
    @field:NotBlank
    @field:Size(max = 120)
    val name: String,

    @field:Schema(
        description = "Form description",
        example = "Get the latest updates directly in your inbox",
        required = true,
    )
    @field:NotBlank
    @field:Size(max = 500)
    val description: String,

    @field:Schema(description = "Whether confirmation is required", example = "true")
    val confirmationRequired: Boolean = true,

    @field:NotBlank(message = "Success action type is required")
    @field:Pattern(regexp = "^(SHOW_MESSAGE|REDIRECT)$")
    @field:Schema(
        description = "Action on success",
        example = "SHOW_MESSAGE",
        allowableValues = ["SHOW_MESSAGE", "REDIRECT"],
    )
    val successActionType: String = "SHOW_MESSAGE",

    @field:Size(max = 500)
    @field:Schema(description = "Success message")
    val successMessage: String? = null,

    @field:Size(max = 2048)
    @field:Schema(description = "Redirect URL")
    val redirectUrl: String? = null,

    // Styling settings
    @field:Schema(description = "Submit button color (hex)", example = "#06B6D4", required = true)
    @field:NotBlank
    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6})$")
    val buttonColor: String,

    @field:Schema(description = "Page background color for iframe body (hex)", example = "#FFFFFF", required = true)
    @field:NotBlank
    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6})$")
    val pageBackgroundColor: String,

    @field:Schema(description = "Background color (hex)", example = "#FFFFFF", required = true)
    @field:NotBlank
    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6})$")
    val backgroundColor: String,

    @field:Schema(description = "Text color (hex)", example = "#000000", required = true)
    @field:NotBlank
    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6})$")
    val textColor: String,

    @field:Schema(description = "Button text color (hex)", example = "#FFFFFF", required = true)
    @field:NotBlank
    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6})$")
    val buttonTextColor: String,

    @field:Schema(description = "Input text color (hex)", example = "#000000", required = true)
    @field:NotBlank
    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6})$")
    val inputTextColor: String,

    @field:Schema(description = "Border color (hex)", example = "#000000", required = true)
    @field:NotBlank
    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6})$")
    val borderColor: String,

    @field:Schema(description = "Border style", example = "solid", required = true)
    @field:NotBlank
    @field:Size(max = 50)
    val borderStyle: String,

    @field:Schema(description = "Box shadow", example = "none", required = true)
    @field:NotBlank
    @field:Size(max = 50)
    val shadow: String,

    @field:Schema(description = "Border thickness in pixels", example = "0", required = true)
    @field:NotNull
    @field:Min(0)
    @field:Max(100)
    val borderThickness: Int,

    @field:Schema(description = "Form width", example = "auto", required = true)
    @field:NotBlank
    @field:Size(max = 50)
    val width: String,

    @field:Schema(description = "Form height", example = "auto", required = true)
    @field:NotBlank
    @field:Size(max = 50)
    val height: String,

    @field:Schema(description = "Horizontal alignment", example = "center", required = true)
    @field:NotBlank
    @field:Size(max = 50)
    val horizontalAlignment: String,

    @field:Schema(description = "Vertical alignment", example = "center", required = true)
    @field:NotBlank
    @field:Size(max = 50)
    val verticalAlignment: String,

    @field:Schema(description = "Padding in pixels", example = "16", required = true)
    @field:NotNull
    @field:Min(0)
    @field:Max(100)
    val padding: Int,

    @field:Schema(description = "Gap between elements", example = "16", required = true)
    @field:NotNull
    @field:Min(0)
    @field:Max(100)
    val gap: Int,

    @field:Schema(description = "Corner radius in pixels", example = "8", required = true)
    @field:NotNull
    @field:Min(0)
    @field:Max(100)
    val cornerRadius: Int,

    // Content settings
    @field:Schema(description = "Whether to show header", example = "true")
    val showHeader: Boolean = true,

    @field:Schema(description = "Whether to show subheader", example = "true")
    val showSubheader: Boolean = true,

    @field:Schema(description = "Header title", example = "Join our newsletter", required = true)
    @field:NotBlank
    @field:Size(max = 120)
    val headerTitle: String,

    @field:Schema(description = "Subheader text", example = "Get the latest updates")
    @field:Size(max = 500)
    val subheaderText: String? = null,

    @field:Schema(description = "Input placeholder", example = "Enter your email", required = true)
    @field:NotBlank
    @field:Size(max = 100)
    val inputPlaceholder: String,

    @field:Schema(description = "Submit button text", example = "Subscribe", required = true)
    @field:NotBlank
    @field:Size(max = 50)
    val submitButtonText: String,

    @field:Schema(description = "Submitting button text", example = "Submitting...", required = true)
    @field:NotBlank
    @field:Size(max = 50)
    val submittingButtonText: String,

    @field:Schema(description = "Whether to show TOS checkbox", example = "false")
    val showTosCheckbox: Boolean = false,

    @field:Schema(description = "TOS text", example = "I agree to the Terms of Service")
    @field:Size(max = 500)
    val tosText: String? = null,

    @field:Schema(description = "Whether to show privacy checkbox", example = "false")
    val showPrivacyCheckbox: Boolean = false,

    @field:Schema(description = "Privacy text", example = "I agree to the Privacy Policy")
    @field:Size(max = 500)
    val privacyText: String? = null,
) {
    fun toCommand(id: UUID, workspaceId: UUID, userId: UUID) = UpdateSubscriberFormCommand(
        id = id,
        name = name,
        description = description,
        confirmationRequired = confirmationRequired,
        successActionType = successActionType,
        successMessage = successMessage,
        redirectUrl = redirectUrl,
        // Styling
        buttonColor = buttonColor,
        pageBackgroundColor = pageBackgroundColor,
        backgroundColor = backgroundColor,
        textColor = textColor,
        buttonTextColor = buttonTextColor,
        inputTextColor = inputTextColor,
        borderColor = borderColor,
        borderStyle = borderStyle,
        shadow = shadow,
        borderThickness = borderThickness,
        width = width,
        height = height,
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = verticalAlignment,
        padding = padding,
        gap = gap,
        cornerRadius = cornerRadius,
        // Content
        showHeader = showHeader,
        showSubheader = showSubheader,
        headerTitle = headerTitle,
        subheaderText = subheaderText,
        inputPlaceholder = inputPlaceholder,
        submitButtonText = submitButtonText,
        submittingButtonText = submittingButtonText,
        showTosCheckbox = showTosCheckbox,
        tosText = tosText,
        showPrivacyCheckbox = showPrivacyCheckbox,
        privacyText = privacyText,
        workspaceId = workspaceId,
        userId = userId,
    )
}
