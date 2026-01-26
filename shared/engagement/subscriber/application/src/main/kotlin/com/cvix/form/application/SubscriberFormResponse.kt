package com.cvix.form.application

import com.cvix.common.domain.bus.query.Response
import com.cvix.form.domain.SubscriptionForm

/**
 * Data class representing a response for a subscription form.
 */
data class SubscriberFormResponse(
    val id: String,
    val name: String,
    val header: String,
    val description: String,
    val inputPlaceholder: String,
    val buttonText: String,
    val buttonColor: String,
    val backgroundColor: String,
    val textColor: String,
    val buttonTextColor: String,
    val workspaceId: String,
    val createdAt: String,
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
