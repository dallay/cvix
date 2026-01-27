package com.cvix.form.infrastructure.http.request

import com.cvix.form.application.update.UpdateSubscriberFormCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.*

/**
 * Request to update a subscription form
 *
 * @property name Form name (max 100 characters)
 * @property header Form header (max 150 characters)
 * @property description Form description (max 1000 characters)
 * @property inputPlaceholder Placeholder text for the input (max 255 characters)
 * @property buttonText Submit button text (max 50 characters)
 * @property buttonColor Hex color for button (required, e.g. #RRGGBB)
 * @property backgroundColor Hex color for background (required, e.g. #RRGGBB)
 * @property textColor Hex color for text (required, e.g. #RRGGBB)
 * @property buttonTextColor Hex color for button text (required, e.g. #RRGGBB)
 * @property confirmationRequired Whether the subscriber needs to confirm their email
 */

@Schema(description = "Request to update a subscription form")
data class UpdateSubscriberFormRequest(
    @field:Schema(description = "Form name", example = "Newsletter Signup", required = true)
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,

    @field:Schema(description = "Form header", example = "Join our newsletter", required = true)
    @field:NotBlank
    @field:Size(max = 150)
    val header: String,

    @field:Schema(
        description = "Form description", example = "Get the latest updates directly in your inbox",
        required = true,
    )
    @field:NotBlank
    @field:Size(max = 1000)
    val description: String,

    @field:Schema(
        description = "Input field placeholder",
        example = "your@email.com",
        required = true,
    )
    @field:NotBlank
    @field:Size(max = 255)
    val inputPlaceholder: String,

    @field:Schema(description = "Submit button text", example = "Subscribe", required = true)
    @field:NotBlank
    @field:Size(max = 50)
    val buttonText: String,

    @field:Schema(description = "Submit button color (hex)", example = "#000000", required = true)
    @field:NotBlank
    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
    val buttonColor: String,

    @field:Schema(description = "Background color (hex)", example = "#ffffff", required = true)
    @field:NotBlank
    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
    val backgroundColor: String,

    @field:Schema(description = "Text color (hex)", example = "#000000", required = true)
    @field:NotBlank
    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
    val textColor: String,

    @field:Schema(description = "Button text color (hex)", example = "#ffffff", required = true)
    @field:NotBlank
    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
    val buttonTextColor: String,

    @field:Schema(description = "Whether confirmation is required", example = "true")
    val confirmationRequired: Boolean = true
) {
    fun toCommand(id: UUID, workspaceId: UUID, userId: UUID) = UpdateSubscriberFormCommand(
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
        confirmationRequired = confirmationRequired,
        workspaceId = workspaceId,
        userId = userId,
    )
}
