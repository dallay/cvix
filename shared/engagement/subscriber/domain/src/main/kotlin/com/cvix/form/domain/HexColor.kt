package com.cvix.form.domain

/**
 * Value object for hexadecimal color codes that are validated when created.
 *
 * This class ensures that the color code is in a valid hex format (with a leading '#',
 * and either 3 or 6 hexadecimal digits). Validation occurs at instantiation.
 *
 * @property value The validated hexadecimal color string (always prefixed with '#').
 * @constructor Creates a [HexColor] after validating the input string.
 */
@JvmInline
value class HexColor(val value: String) {
    init {
        require(value.isNotBlank() && value != "#") { "Color code cannot be empty" }
        require(regex.matches(value)) { "Invalid hexadecimal color code: $value" }
    }

    /**
     * Returns the string representation of the hex color value.
     * @return the hex color string
     */
    override fun toString(): String = value

    companion object {
        /**
         * Regex pattern that matches hex colors:
         * - Mandatory # prefix
         * - Either 3 or 6 hexadecimal digits (case-insensitive)
         */
        val regex = Regex("^#([0-9a-f]{6}|[0-9a-f]{3})$", RegexOption.IGNORE_CASE)

        /**
         * Factory method to create a [HexColor] from a string, adding '#' if missing.
         * @param hex The input hex string (with or without '#').
         * @return a validated [HexColor] instance
         */
        fun from(hex: String): HexColor =
            HexColor(if (hex.startsWith("#")) hex else "#$hex")
    }
}
