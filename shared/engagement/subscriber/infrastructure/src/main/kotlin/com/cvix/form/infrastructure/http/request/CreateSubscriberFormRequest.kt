package com.cvix.form.infrastructure.http.request

import com.cvix.form.application.create.ContentInput
import com.cvix.form.application.create.CreateSubscriberFormCommand
import com.cvix.form.application.create.StylingInput
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.*

/**
 * Request to create a new subscription form.
 *
 * This DTO defines the initial configuration for a subscription form,
 * including its visual style and public-facing content.
 *
 * @property name Internal name of the form (required, max 120 chars).
 * @property description Detailed description or call to action (required, max 500 chars).
 * @property confirmationRequired Whether the subscriber needs to confirm their email (default true).
 * @property successActionType Action on success: 'SHOW_MESSAGE' or 'REDIRECT'.
 * @property successMessage Message shown on success (when successActionType is SHOW_MESSAGE).
 * @property redirectUrl URL to redirect to (when successActionType is REDIRECT).
 */
@Schema(description = "Request to create a subscription form")
data class CreateSubscriberFormRequest(
    @field:NotBlank(message = "Form name is required")
    @field:Size(max = 120, message = "Form name must be at most 120 characters")
    @field:Schema(description = "Form name", example = "Newsletter Signup", maxLength = 120)
    val name: String,

    @field:NotBlank(message = "Form description is required")
    @field:Size(max = 500, message = "Form description must be at most 500 characters")
    @field:Schema(
        description = "Form description", example = "Get the latest updates directly in your inbox",
        maxLength = 500,
    )
    val description: String,

    @field:Schema(description = "Whether confirmation is required", example = "true")
    val confirmationRequired: Boolean = true,

    @field:NotBlank(message = "Success action type is required")
    @field:Pattern(
        regexp = "^(SHOW_MESSAGE|REDIRECT)$",
        message = "Success action type must be SHOW_MESSAGE or REDIRECT",
    )
    @field:Schema(
        description = "Action on success",
        example = "SHOW_MESSAGE",
        allowableValues = ["SHOW_MESSAGE", "REDIRECT"],
    )
    val successActionType: String = "SHOW_MESSAGE",

    @field:Size(max = 500, message = "Success message must be at most 500 characters")
    @field:Schema(description = "Success message", example = "Success! Check your email to confirm.")
    val successMessage: String? = "Success! Now check your email to confirm your subscription.",

    @field:Size(max = 2048, message = "Redirect URL must be at most 2048 characters")
    @field:Schema(description = "Redirect URL", example = "https://example.com/thank-you")
    val redirectUrl: String? = null,

    // Styling settings
    @field:NotBlank(message = "Button color is required")
    @field:Pattern(
        regexp = "^#([A-Fa-f0-9]{6})$",
        message = "Button color must be a valid hex code (e.g., #000000)",
    )
    @field:Schema(description = "Submit button color (hex)", example = "#06B6D4")
    val buttonColor: String = "#06B6D4",

    @field:NotBlank(message = "Page background color is required")
    @field:Pattern(
        regexp = HEX_COLOR_PATTERN,
        message = "Page background color must be a valid hex code (e.g., #ffffff)",
    )
    @field:Schema(description = "Page background color for iframe body (hex)", example = DEFAULT_WHITE)
    val pageBackgroundColor: String = DEFAULT_WHITE,

    @field:NotBlank(message = BG_COLOR_REQUIRED_MSG)
    @field:Pattern(
        regexp = HEX_COLOR_PATTERN,
        message = HEX_COLOR_VALIDATION_MSG,
    )
    @field:Schema(description = "Background color (hex)", example = DEFAULT_WHITE)
    val backgroundColor: String = DEFAULT_WHITE,

    @field:NotBlank(message = "Text color is required")
    @field:Pattern(
        regexp = "^#([A-Fa-f0-9]{6})$",
        message = "Text color must be a valid hex code (e.g., #000000)",
    )
    @field:Schema(description = "Text color (hex)", example = DEFAULT_BLACK)
    val textColor: String = DEFAULT_BLACK,

    @field:NotBlank(message = "Button text color is required")
    @field:Pattern(
        regexp = "^#([A-Fa-f0-9]{6})$",
        message = "Button text color must be a valid hex code (e.g., #ffffff)",
    )
    @field:Schema(description = "Button text color (hex)", example = DEFAULT_WHITE)
    val buttonTextColor: String = DEFAULT_WHITE,

    @field:NotBlank(message = "Input text color is required")
    @field:Pattern(
        regexp = "^#([A-Fa-f0-9]{6})$",
        message = "Input text color must be a valid hex code (e.g., #000000)",
    )
    @field:Schema(description = "Input text color (hex)", example = DEFAULT_BLACK)
    val inputTextColor: String = DEFAULT_BLACK,

    @field:NotBlank(message = "Border color is required")
    @field:Pattern(
        regexp = "^#([A-Fa-f0-9]{6})$",
        message = "Border color must be a valid hex code (e.g., #000000)",
    )
    @field:Schema(description = "Border color (hex)", example = DEFAULT_BLACK)
    val borderColor: String = DEFAULT_BLACK,

    @field:NotBlank(message = "Border style is required")
    @field:Size(max = 50, message = "Border style must be at most 50 characters")
    @field:Schema(description = "Border style", example = "solid")
    val borderStyle: String = "solid",

    @field:NotBlank(message = "Shadow is required")
    @field:Size(max = 50, message = "Shadow must be at most 50 characters")
    @field:Schema(description = "Box shadow", example = "none")
    val shadow: String = "none",

    @field:NotNull(message = "Border thickness is required")
    @field:Min(0, message = "Border thickness must be non-negative")
    @field:Max(100, message = "Border thickness must be at most 100")
    @field:Schema(description = "Border thickness in pixels", example = "0")
    val borderThickness: Int = 0,

    @field:NotBlank(message = "Width is required")
    @field:Size(max = 50, message = "Width must be at most 50 characters")
    @field:Schema(description = "Form width", example = "auto")
    val width: String = "auto",

    @field:NotBlank(message = "Height is required")
    @field:Size(max = 50, message = "Height must be at most 50 characters")
    @field:Schema(description = "Form height", example = "auto")
    val height: String = "auto",

    @field:NotBlank(message = "Horizontal alignment is required")
    @field:Size(max = 50, message = "Horizontal alignment must be at most 50 characters")
    @field:Schema(description = "Horizontal alignment", example = "center")
    val horizontalAlignment: String = "center",

    @field:NotBlank(message = "Vertical alignment is required")
    @field:Size(max = 50, message = "Vertical alignment must be at most 50 characters")
    @field:Schema(description = "Vertical alignment", example = "center")
    val verticalAlignment: String = "center",

    @field:NotNull(message = "Padding is required")
    @field:Min(0, message = "Padding must be non-negative")
    @field:Max(100, message = "Padding must be at most 100")
    @field:Schema(description = "Padding in pixels", example = "16")
    val padding: Int = 16,

    @field:NotNull(message = "Gap is required")
    @field:Min(0, message = "Gap must be non-negative")
    @field:Max(100, message = "Gap must be at most 100")
    @field:Schema(description = "Gap between elements in pixels", example = "16")
    val gap: Int = 16,

    @field:NotNull(message = "Corner radius is required")
    @field:Min(0, message = "Corner radius must be non-negative")
    @field:Max(100, message = "Corner radius must be at most 100")
    @field:Schema(description = "Corner radius in pixels", example = "8")
    val cornerRadius: Int = 8,

    // Content settings
    @field:Schema(description = "Whether to show header", example = "true")
    val showHeader: Boolean = true,

    @field:Schema(description = "Whether to show subheader", example = "true")
    val showSubheader: Boolean = true,

    @field:NotBlank(message = "Header title is required")
    @field:Size(max = 120, message = "Header title must be at most 120 characters")
    @field:Schema(description = "Header title", example = "Join our newsletter")
    val headerTitle: String = "Join our newsletter",

    @field:Size(max = 500, message = "Subheader text must be at most 500 characters")
    @field:Schema(description = "Subheader text", example = "Get the latest updates")
    val subheaderText: String? = null,

    @field:NotBlank(message = "Input placeholder is required")
    @field:Size(max = 100, message = "Input placeholder must be at most 100 characters")
    @field:Schema(description = "Input placeholder", example = "Enter your email")
    val inputPlaceholder: String = "Enter your email",

    @field:NotBlank(message = "Submit button text is required")
    @field:Size(max = 50, message = "Submit button text must be at most 50 characters")
    @field:Schema(description = "Submit button text", example = "Subscribe")
    val submitButtonText: String = "Subscribe",

    @field:NotBlank(message = "Submitting button text is required")
    @field:Size(max = 50, message = "Submitting button text must be at most 50 characters")
    @field:Schema(description = "Submitting button text", example = "Submitting...")
    val submittingButtonText: String = "Submitting...",

    @field:Schema(description = "Whether to show TOS checkbox", example = "false")
    val showTosCheckbox: Boolean = false,

    @field:Size(max = 500, message = "TOS text must be at most 500 characters")
    @field:Schema(description = "TOS text", example = "I agree to the Terms of Service")
    val tosText: String? = null,

    @field:Schema(description = "Whether to show privacy checkbox", example = "false")
    val showPrivacyCheckbox: Boolean = false,

    @field:Size(max = 500, message = "Privacy text must be at most 500 characters")
    @field:Schema(description = "Privacy text", example = "I agree to the Privacy Policy")
    val privacyText: String? = null,
) {
    fun toCommand(id: UUID, workspaceId: UUID, userId: UUID) = CreateSubscriberFormCommand(
        id = id,
        name = name,
        description = description,
        confirmationRequired = confirmationRequired,
        successActionType = successActionType,
        successMessage = successMessage,
        redirectUrl = redirectUrl,
        styling = StylingInput(
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
        ),
        content = ContentInput(
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
        ),
        workspaceId = workspaceId,
        userId = userId,
    )

    companion object {
        private const val HEX_COLOR_PATTERN = "^#([A-Fa-f0-9]{6})$"
        private const val BG_COLOR_REQUIRED_MSG = "Background color is required"
        private const val HEX_COLOR_VALIDATION_MSG = "Background color must be a valid hex code (e.g., #ffffff)"
        private const val DEFAULT_WHITE = "#FFFFFF"
        private const val DEFAULT_BLACK = "#000000"
    }
}
