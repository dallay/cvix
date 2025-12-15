package com.cvix.waitlist.domain

/**
 * Enum representing supported languages for waitlist entries.
 */
enum class Language(val code: String) {
    ENGLISH("en"),
    SPANISH("es");

    companion object {
        fun fromString(code: String): Language =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
    }
}
