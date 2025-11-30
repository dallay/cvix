package com.cvix.resume.domain

/**
 * Entity representing an interest in a resume.
 */
data class Interest(
    val name: String,
    val keywords: List<String>? = null
) {
    init {
        require(name.isNotBlank()) { "Interest name cannot be blank" }
    }
}
