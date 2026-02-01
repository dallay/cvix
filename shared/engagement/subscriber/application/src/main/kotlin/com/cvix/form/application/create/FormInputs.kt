package com.cvix.form.application.create

/**
 * Input data class for styling properties of a subscriber form.
 *
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
 */
data class StylingInput(
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
)

/**
 * Input data class for content properties of a subscriber form.
 *
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
 */
data class ContentInput(
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
)
