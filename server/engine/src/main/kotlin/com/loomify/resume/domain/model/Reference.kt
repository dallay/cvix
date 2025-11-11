package com.loomify.resume.domain.model

/**
 * Entity representing a reference in a resume.
 */
data class Reference(
    val name: String,
    val reference: String? = null
) {
    init {
        require(name.isNotBlank()) { "Reference name cannot be blank" }
    }
}
