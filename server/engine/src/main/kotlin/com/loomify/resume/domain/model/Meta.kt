package com.loomify.resume.domain.model

/**
 * Entity representing schema metadata in a resume per JSON Resume Schema.
 */
data class Meta(
    val canonical: String? = null,
    val version: String? = null,
    val lastModified: String? = null
)
