package com.loomify.resume.domain.model

/**
 * Entity representing a language proficiency in a resume.
 * Fluency is stored as a free-form string per JSON Resume Schema.
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
