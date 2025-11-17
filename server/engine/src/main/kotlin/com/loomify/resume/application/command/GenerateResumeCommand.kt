package com.loomify.resume.application.command

import com.loomify.resume.domain.Resume

/**
 * Command to generate a PDF resume from resume data.
 * Part of the CQRS pattern in the application layer.
 *
 * Supported locales: "en", "es"
 */
data class GenerateResumeCommand(
    val resume: Resume,
    val locale: Locale = Locale.EN
)

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
