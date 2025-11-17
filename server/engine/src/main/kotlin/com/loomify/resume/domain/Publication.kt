package com.loomify.resume.domain

/**
 * Entity representing a publication in a resume.
 */
data class Publication(
    val name: String,
    val publisher: String? = null,
    val releaseDate: String? = null,
    val url: Url? = null,
    val summary: String? = null
) {
    init {
        require(name.isNotBlank()) { "Publication name cannot be blank" }
    }
}
