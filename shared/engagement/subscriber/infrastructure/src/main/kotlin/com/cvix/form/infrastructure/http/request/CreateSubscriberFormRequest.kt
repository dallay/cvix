package com.cvix.form.infrastructure.http.request

import com.cvix.form.application.create.CreateSubscriberFormCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
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
 * @property header Public-facing header text (required, max 120 chars).
 * @property description Detailed description or call to action (required, max 500 chars).
 * @property inputPlaceholder Placeholder for the email field (required, max 100 chars).
 * @property buttonText Text displayed on the button (required, max 50 chars).
 * @property buttonColor Button background hex color (required, e.g., #000000).
 * @property backgroundColor Form background hex color (required, e.g., #ffffff).
 * @property textColor Main text hex color (required, e.g., #000000).
 * @property buttonTextColor Button text hex color (required, e.g., #ffffff).
 */
@Schema(description = "Request to create a subscription form")
data class CreateSubscriberFormRequest(
    @field:NotBlank(message = "Form name is required")
    @field:Size(max = 120, message = "Form name must be at most 120 characters")
    @field:Schema(description = "Form name", example = "Newsletter Signup", maxLength = 120)
    val name: String,

    @field:NotBlank(message = "Form header is required")
    @field:Size(max = 120, message = "Form header must be at most 120 characters")
    @field:Schema(description = "Form header", example = "Join our newsletter", maxLength = 120)
    val header: String,

    @field:NotBlank(message = "Form description is required")
    @field:Size(max = 500, message = "Form description must be at most 500 characters")
    @field:Schema(
        description = "Form description", example = "Get the latest updates directly in your inbox",
        maxLength = 500,
    )
    val description: String,

    @field:NotBlank(message = "Input placeholder is required")
    @field:Size(max = 100, message = "Input placeholder must be at most 100 characters")
    @field:Schema(
        description = "Input field placeholder",
        example = "your@email.com",
        maxLength = 100,
    )
    val inputPlaceholder: String,

    @field:NotBlank(message = "Button text is required")
    @field:Size(max = 50, message = "Button text must be at most 50 characters")
    @field:Schema(description = "Submit button text", example = "Subscribe", maxLength = 50)
    val buttonText: String,

    @field:NotBlank(message = "Button color is required")
    @field:Pattern(
        regexp = "^#([A-Fa-f0-9]{6})$",
        message = "Button color must be a valid hex code (e.g., #000000)",
    )
    @field:Schema(
        description = "Submit button color (hex)",
        example = "#000000",
        pattern = "^#([A-Fa-f0-9]{6})$",
    )
    val buttonColor: String,

    @field:NotBlank(message = "Background color is required")
    @field:Pattern(
        regexp = "^#([A-Fa-f0-9]{6})$",
        message = "Background color must be a valid hex code (e.g., #ffffff)",
    )
    @field:Schema(
        description = "Background color (hex)",
        example = "#ffffff",
        pattern = "^#([A-Fa-f0-9]{6})$",
    )
    val backgroundColor: String,

    @field:NotBlank(message = "Text color is required")
    @field:Pattern(
        regexp = "^#([A-Fa-f0-9]{6})$",
        message = "Text color must be a valid hex code (e.g., #000000)",
    )
    @field:Schema(
        description = "Text color (hex)",
        example = "#000000",
        pattern = "^#([A-Fa-f0-9]{6})$",
    )
    val textColor: String,

    @field:NotBlank(message = "Button text color is required")
    @field:Pattern(
        regexp = "^#([A-Fa-f0-9]{6})$",
        message = "Button text color must be a valid hex code (e.g., #ffffff)",
    )
    @field:Schema(
        description = "Button text color (hex)",
        example = "#ffffff",
        pattern = "^#([A-Fa-f0-9]{6})$",
    )
    val buttonTextColor: String
) {
    fun toCommand(id: UUID, workspaceId: UUID, userId: UUID) = CreateSubscriberFormCommand(
        id = id,
        name = name,
        header = header,
        description = description,
        inputPlaceholder = inputPlaceholder,
        buttonText = buttonText,
        buttonColor = buttonColor,
        backgroundColor = backgroundColor,
        textColor = textColor,
        buttonTextColor = buttonTextColor,
        workspaceId = workspaceId,
        userId = userId,
    )
}
