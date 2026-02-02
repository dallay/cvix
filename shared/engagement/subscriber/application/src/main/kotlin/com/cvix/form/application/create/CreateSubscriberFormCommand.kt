package com.cvix.form.application.create

import com.cvix.common.domain.bus.command.Command
import java.util.*

/**
 * Command to create a subscriber form.
 *
 * This command is carried over the application's command bus and represents
 * the intent to create a new subscriber-facing form with presentation and
 * ownership metadata.
 *
 * @property id Unique identifier for the subscriber form to be created.
 * @property name Visible title of the form shown to end users.
 * @property description Optional longer description or subtitle for the form.
 * @property confirmationRequired Whether the subscriber needs to confirm their email.
 * @property successActionType Action on success: SHOW_MESSAGE or REDIRECT.
 * @property successMessage Message shown on success.
 * @property redirectUrl URL to redirect to when successActionType is REDIRECT.
 * @property styling Styling configuration for the form.
 * @property content Content configuration for the form.
 * @property workspaceId Identifier of the workspace that owns the form.
 * @property userId Identifier of the user creating or requesting the creation of the form.
 */
data class CreateSubscriberFormCommand(
    val id: UUID,
    val name: String,
    val description: String,
    val confirmationRequired: Boolean,
    val successActionType: String,
    val successMessage: String?,
    val redirectUrl: String?,
    val styling: StylingInput,
    val content: ContentInput,
    val workspaceId: UUID,
    val userId: UUID,
) : Command
