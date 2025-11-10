package com.loomify.resume.domain.model

/**
 * Entity representing a publication in a resume per JSON Resume Schema.
 */
data class Publication(
    val name: String,
    val publisher: String? = null,
    val releaseDate: String? = null,
    val url: Url? = null,
    val summary: String? = null
) {
    init {
        require(name.isNotBlank()) { "Publication name must not be blank" }
    }
}
