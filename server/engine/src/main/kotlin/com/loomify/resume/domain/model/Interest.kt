package com.loomify.resume.domain.model

/**
 * Entity representing an interest in a resume per JSON Resume Schema.
 */
data class Interest(
    val name: String,
    val keywords: List<String>? = null
) {
    init {
        require(name.isNotBlank()) { "Interest name must not be blank" }
    }
}
