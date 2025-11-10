package com.loomify.resume.domain.model

/**
 * Entity representing a language proficiency in a resume.
 * Per JSON Resume Schema, fluency accepts free-form text (e.g., "Fluent", "Native speaker", "Professional working proficiency")
 */
data class Language(
    val language: String,
    val fluency: String
) {
    init {
        require(language.isNotBlank()) { "Language must not be blank" }
        require(fluency.isNotBlank()) { "Fluency must not be blank" }
    }
}
