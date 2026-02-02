package com.cvix.form.application.update

import com.cvix.common.domain.bus.command.Command
import java.util.*

/**
 * Command to update an existing subscriber form.
 *
 * @property id Unique identifier for the subscriber form.
 * @property name Visible title of the form.
 * @property description Description or subtitle for the form.
 * @property confirmationRequired Whether the subscriber needs to confirm their email.
 * @property successActionType Action on success: SHOW_MESSAGE or REDIRECT.
 * @property successMessage Message shown on success.
 * @property redirectUrl URL to redirect to when successActionType is REDIRECT.
 * @property buttonColor Background color for the submit button (hex code).
 * @property pageBackgroundColor Page background color for iframe body (hex code).
 * @property backgroundColor Background color for the form container (hex code).
 * @property textColor Primary text color used within the form (hex code).
 * @property buttonTextColor Text color used on the submit button (hex code).
 * @property inputTextColor Text color used for the email input field (hex code).
 * @property borderColor Border color for the form (hex code).
 * @property borderStyle Border style (e.g., solid, dashed).
 * @property shadow Box shadow style.
 * @property borderThickness Border thickness in pixels.
 * @property width Form width (e.g., auto, fit, or custom value).
 * @property height Form height (e.g., auto, fit, or custom value).
 * @property horizontalAlignment Horizontal alignment (left, center, right).
 * @property verticalAlignment Vertical alignment (top, center, bottom).
 * @property padding Internal padding in pixels.
 * @property gap Gap between elements in pixels.
 * @property cornerRadius Border radius in pixels.
 * @property showHeader Whether to show the header.
 * @property showSubheader Whether to show the subheader.
 * @property headerTitle Header title text.
 * @property subheaderText Subheader text (optional).
 * @property inputPlaceholder Placeholder text for the email input.
 * @property submitButtonText Button text.
 * @property submittingButtonText Button text during submission.
 * @property showTosCheckbox Whether to show Terms of Service checkbox.
 * @property tosText Terms of Service text.
 * @property showPrivacyCheckbox Whether to show Privacy Policy checkbox.
 * @property privacyText Privacy Policy text.
 * @property workspaceId Identifier of the workspace that owns the form.
 * @property userId Identifier of the user updating the form.
 */
data class UpdateSubscriberFormCommand(
    val id: UUID,
    val name: String,
    val description: String,
    val confirmationRequired: Boolean,
    val successActionType: String,
    val successMessage: String?,
    val redirectUrl: String?,
    // Styling
    val buttonColor: String,
    val pageBackgroundColor: String,
    val backgroundColor: String,
    val textColor: String,
    val buttonTextColor: String,
    val inputTextColor: String,
    val borderColor: String,
    val borderStyle: String,
    val shadow: String,
    val borderThickness: Int,
    val width: String,
    val height: String,
    val horizontalAlignment: String,
    val verticalAlignment: String,
    val padding: Int,
    val gap: Int,
    val cornerRadius: Int,
    // Content
    val showHeader: Boolean,
    val showSubheader: Boolean,
    val headerTitle: String,
    val subheaderText: String?,
    val inputPlaceholder: String,
    val submitButtonText: String,
    val submittingButtonText: String,
    val showTosCheckbox: Boolean,
    val tosText: String?,
    val showPrivacyCheckbox: Boolean,
    val privacyText: String?,
    val workspaceId: UUID,
    val userId: UUID,
) : Command
