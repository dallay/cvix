package com.cvix.form.application.create

import com.cvix.common.domain.bus.command.Command
import java.util.UUID

/**
 * Command to create a subscriber form.
 *
 * This command is carried over the application's command bus and represents
 * the intent to create a new subscriber-facing form with presentation and
 * ownership metadata.
 *
 * @property id Unique identifier for the subscriber form to be created.
 * @property name Visible title of the form shown to end users.
 * @property header Short header text displayed at the top of the form.
 * @property description Optional longer description or subtitle for the form.
 * @property inputPlaceholder Placeholder text for the primary input field (e.g. email).
 * @property buttonText Text displayed on the submit button.
 * @property buttonColor Background color for the submit button (hex code or token).
 * @property backgroundColor Background color for the form container (hex code or token).
 * @property textColor Primary text color used within the form (hex code or token).
 * @property buttonTextColor Text color used on the submit button (hex code or token).
 * @property workspaceId Identifier of the workspace that owns the form.
 * @property userId Identifier of the user creating or requesting the creation of the form.
 */
data class CreateSubscriberFormCommand (
    val id: UUID,
    val name: String,
    val header: String,
    val description: String,
    val inputPlaceholder: String,
    val buttonText: String,
    val buttonColor: String,
    val backgroundColor: String,
    val textColor: String,
    val buttonTextColor: String,
    val workspaceId: UUID,
    val userId: UUID,
) : Command
