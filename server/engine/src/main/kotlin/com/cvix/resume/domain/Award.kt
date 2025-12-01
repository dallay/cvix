package com.cvix.resume.domain

/**
 * Entity representing an award entry in a resume.
 */
data class Award(
    val title: String,
    val date: String? = null,
    val awarder: String? = null,
    val summary: String? = null
) {
    init {
        require(title.isNotBlank()) { "Title cannot be blank" }
    }
}
