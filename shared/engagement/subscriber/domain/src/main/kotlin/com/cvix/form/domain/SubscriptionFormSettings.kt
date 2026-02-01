package com.cvix.form.domain

/**
 * Data class representing the complete configuration for a subscription form.
 * Organized into three main sections: Settings, Styling, and Content.
 *
 * @property settings Behavior settings (success action, confirmation)
 * @property styling Visual styling configuration
 * @property content Content and visibility configuration
 */
data class SubscriptionFormSettings(
    val settings: FormBehaviorSettings,
    val styling: FormStylingSettings,
    val content: FormContentSettings,
) {
    init {
        // Validation is handled by nested classes
    }

    companion object {
        /**
         * Creates default settings for a new subscription form.
         */
        fun defaults(): SubscriptionFormSettings = SubscriptionFormSettings(
            settings = FormBehaviorSettings.defaults(),
            styling = FormStylingSettings.defaults(),
            content = FormContentSettings.defaults(),
        )
    }
}

/**
 * Behavior settings controlling form actions after submission.
 *
 * @property successActionType How to handle successful submission ('SHOW_MESSAGE' or 'REDIRECT')
 * @property successMessage Message shown on success (when successActionType is 'SHOW_MESSAGE')
 * @property redirectUrl URL to redirect to (when successActionType is 'REDIRECT')
 * @property confirmationRequired Whether double opt-in is required
 */
data class FormBehaviorSettings(
    val successActionType: SuccessActionType,
    val successMessage: String?,
    val redirectUrl: String?,
    val confirmationRequired: Boolean,
) {
    init {
        require(successActionType == SuccessActionType.SHOW_MESSAGE || !redirectUrl.isNullOrBlank()) {
            "redirectUrl is required when successActionType is REDIRECT"
        }
    }

    companion object {
        fun defaults(): FormBehaviorSettings = FormBehaviorSettings(
            successActionType = SuccessActionType.SHOW_MESSAGE,
            successMessage = "Success! Now check your email to confirm your subscription.",
            redirectUrl = null,
            confirmationRequired = true,
        )
    }
}

enum class SuccessActionType {
    SHOW_MESSAGE,
    REDIRECT,
}

/**
 * Visual styling configuration for the form.
 *
 * @property pageBackgroundColor Page background color for iframe body (hex)
 * @property backgroundColor Background color (hex)
 * @property textColor Text color (hex)
 * @property buttonColor Button background color (hex)
 * @property buttonTextColor Button text color (hex)
 * @property borderColor Border color (hex)
 * @property borderStyle Border style (solid, dashed, etc.)
 * @property shadow Box shadow style
 * @property borderThickness Border thickness in pixels
 * @property width Form width ('auto', 'fit', or custom value)
 * @property height Form height ('auto', 'fit', or custom value)
 * @property horizontalAlignment Horizontal alignment (left, center, right)
 * @property verticalAlignment Vertical alignment (top, center, bottom)
 * @property padding Internal padding in pixels
 * @property gap Gap between elements in pixels
 * @property cornerRadius Border radius in pixels
 */
data class FormStylingSettings(
    val pageBackgroundColor: HexColor,
    val backgroundColor: HexColor,
    val textColor: HexColor,
    val buttonColor: HexColor,
    val buttonTextColor: HexColor,
    val inputTextColor: HexColor,
    val borderColor: HexColor,
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
) {
    init {
        require(borderThickness >= 0) { "borderThickness must be non-negative" }
        require(padding >= 0) { "padding must be non-negative" }
        require(gap >= 0) { "gap must be non-negative" }
        require(cornerRadius >= 0) { "cornerRadius must be non-negative" }
    }

    companion object {
        private const val DEFAULT_WHITE = "#FFFFFF"
        private const val DEFAULT_BLACK = "#000000"

        fun defaults(): FormStylingSettings = FormStylingSettings(
            pageBackgroundColor = HexColor(DEFAULT_WHITE),
            backgroundColor = HexColor(DEFAULT_WHITE),
            textColor = HexColor(DEFAULT_BLACK),
            buttonColor = HexColor("#06B6D4"),
            buttonTextColor = HexColor(DEFAULT_WHITE),
            inputTextColor = HexColor(DEFAULT_BLACK),
            borderColor = HexColor(DEFAULT_BLACK),
            borderStyle = "solid",
            shadow = "none",
            borderThickness = 0,
            width = "auto",
            height = "auto",
            horizontalAlignment = "center",
            verticalAlignment = "center",
            padding = 16,
            gap = 16,
            cornerRadius = 8,
        )
    }
}

/**
 * Content configuration and visibility settings.
 *
 * @property showHeader Whether to show the header title
 * @property showSubheader Whether to show the subheader/description
 * @property headerTitle Header/title text
 * @property subheaderText Subheader/description text (optional)
 * @property inputPlaceholder Placeholder text for email input
 * @property submitButtonText Button text
 * @property submittingButtonText Button text during submission
 * @property showTosCheckbox Whether to show Terms of Service checkbox
 * @property tosText Terms of Service text/label
 * @property showPrivacyCheckbox Whether to show Privacy Policy checkbox
 * @property privacyText Privacy Policy text/label
 */
data class FormContentSettings(
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
) {
    init {
        require(headerTitle.isNotBlank()) { "headerTitle must not be blank" }
        require(inputPlaceholder.isNotBlank()) { "inputPlaceholder must not be blank" }
        require(submitButtonText.isNotBlank()) { "submitButtonText must not be blank" }
        require(submittingButtonText.isNotBlank()) { "submittingButtonText must not be blank" }
    }

    companion object {
        fun defaults(): FormContentSettings = FormContentSettings(
            showHeader = true,
            showSubheader = true,
            headerTitle = "Join our newsletter",
            subheaderText = null,
            inputPlaceholder = "Enter your email",
            submitButtonText = "Subscribe",
            submittingButtonText = "Submitting...",
            showTosCheckbox = false,
            tosText = null,
            showPrivacyCheckbox = false,
            privacyText = null,
        )
    }
}
