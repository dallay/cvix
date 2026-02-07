package com.cvix.resume.domain

/**
 * Enum representing supported locales for resume generation.
 */
enum class Locale(val code: String) {
    EN("en"),
    ES("es");

    companion object {
        fun from(code: String): Locale {
            return entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unsupported locale: $code")
        }
    }
}
