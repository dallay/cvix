package com.loomify.resume.domain.model

/**
 * Entity representing a volunteer experience entry in a resume.
 * Dates are stored as ISO-8601 strings (YYYY-MM-DD).
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
        require(organization.isNotBlank()) { "Organization cannot be blank" }
        require(position.isNotBlank()) { "Position cannot be blank" }
    }
}
