package com.cvix.resume.domain

/**
 * Entity representing a certificate in a resume.
 */
data class Certificate(
    val name: String,
    val date: String? = null,
    val url: Url? = null,
    val issuer: String? = null
) {
    init {
        require(name.isNotBlank()) { "Certificate name cannot be blank" }
    }
}
