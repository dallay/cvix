package com.cvix.form.domain

/**
 * Data class representing the customizable settings for a subscription form.
 *
 * @property header The header text displayed at the top of the form.
 * @property inputPlaceholder The placeholder text for the input field.
 * @property buttonText The text displayed on the submit button.
 * @property buttonColor The background color of the submit button, as a validated hexadecimal color.
 * @property backgroundColor The background color of the form, as a validated hexadecimal color.
 * @property textColor The color of the form's text, as a validated hexadecimal color.
 * @property buttonTextColor The color of the button's text, as a validated hexadecimal color.
 */
data class SubscriptionFormSettings (
    val header: String,
    val inputPlaceholder: String,
    val buttonText: String,
    val buttonColor: HexColor,
    val backgroundColor: HexColor,
    val textColor: HexColor,
    val buttonTextColor: HexColor,
)
