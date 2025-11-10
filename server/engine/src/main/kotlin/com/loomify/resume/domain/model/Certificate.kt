package com.loomify.resume.domain.model

/**
 * Entity representing a certificate in a resume per JSON Resume Schema.
 */
data class Certificate(
    val name: String,
    val date: String? = null,
    val url: Url? = null,
    val issuer: String? = null
) {
    init {
        require(name.isNotBlank()) { "Certificate name must not be blank" }
    }
}
