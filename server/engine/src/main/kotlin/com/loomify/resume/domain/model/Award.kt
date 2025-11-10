package com.loomify.resume.domain.model

/**
 * Entity representing an award in a resume per JSON Resume Schema.
 */
data class Award(
    val title: String,
    val date: String? = null,
    val awarder: String? = null,
    val summary: String? = null
) {
    init {
        require(title.isNotBlank()) { "Award title must not be blank" }
    }
}
