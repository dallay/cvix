package com.cvix.form.infrastructure.http.request

import com.cvix.form.application.update.UpdateSubscriberFormCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(description = "Request to update a subscription form")
data class UpdateSubscriberFormRequest(
    @field:Schema(description = "Form name", example = "Newsletter Signup")
    val name: String,
    @field:Schema(description = "Form header", example = "Join our newsletter")
    val header: String,
    @field:Schema(description = "Form description", example = "Get the latest updates directly in your inbox")
    val description: String,
    @field:Schema(description = "Input field placeholder", example = "your@email.com")
    val inputPlaceholder: String,
    @field:Schema(description = "Submit button text", example = "Subscribe")
    val buttonText: String,
    @field:Schema(description = "Submit button color (hex)", example = "#000000")
    val buttonColor: String,
    @field:Schema(description = "Background color (hex)", example = "#ffffff")
    val backgroundColor: String,
    @field:Schema(description = "Text color (hex)", example = "#000000")
    val textColor: String,
    @field:Schema(description = "Button text color (hex)", example = "#ffffff")
    val buttonTextColor: String
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
        workspaceId = workspaceId,
        userId = userId,
    )
}
