package com.cvix.waitlist.domain

/**
 * Enum representing supported languages for waitlist entries.
 *
 * Use [fromString] to obtain the enum from a code.
 *
 * @throws IllegalArgumentException if code is not supported.
 */
enum class Language(val code: String) {
    ENGLISH("en"),
    SPANISH("es");

    companion object {
        /**
         * Obtains the Language instance for a given ISO 639-1 code (e.g. "en", "es").
         * Throws [IllegalArgumentException] if the code is not supported.
         * This is a strict mapping: no fallback or silent defaulting.
         *
         * @param code Language code (e.g., "en", "es")
         * @throws IllegalArgumentException if input does not match a supported code
         */
        fun fromString(code: String?): Language =
            entries.find { it.code.equals(code, ignoreCase = true) }
                ?: throw IllegalArgumentException(
                    "Invalid language code '$code'. Supported codes: ${entries.joinToString { it.code }}",
                )
    }
}
