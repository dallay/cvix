package com.loomify.resume.domain.model

/**
 * Entity representing a language proficiency in a resume.
 */

enum class FluencyLevel {
    NATIVE,
    FLUENT,
    INTERMEDIATE,
    BEGINNER
}

data class Language(
    val language: String,
    val fluency: FluencyLevel
) {
    init {
        require(language.isNotBlank()) { "Language must not be blank" }
    }
}
