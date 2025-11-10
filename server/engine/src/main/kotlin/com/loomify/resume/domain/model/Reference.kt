package com.loomify.resume.domain.model

/**
 * Entity representing a reference in a resume per JSON Resume Schema.
 */
data class Reference(
    val name: String,
    val reference: String? = null
) {
    init {
        require(name.isNotBlank()) { "Reference name must not be blank" }
    }
}
