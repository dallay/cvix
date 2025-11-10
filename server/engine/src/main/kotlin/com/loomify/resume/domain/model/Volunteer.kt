package com.loomify.resume.domain.model

/**
 * Entity representing volunteer work in a resume per JSON Resume Schema.
 * Dates are stored as ISO-8601 strings (YYYY-MM-DD, YYYY-MM, or YYYY).
 */
data class Volunteer(
    val organization: String,
    val position: String,
    val url: Url? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val summary: String? = null,
    val highlights: List<String>? = null
) {
    init {
        require(organization.isNotBlank()) { "Organization must not be blank" }
        require(position.isNotBlank()) { "Position must not be blank" }
    }
}
