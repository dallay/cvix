package com.loomify.resume.domain.model

import java.time.LocalDate

/**
 * Entity representing a project in a resume per JSON Resume Schema.
 * Contains project details and optional date range.
 */
data class Project(
    val name: String,
    val description: String,
    val url: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val highlights: List<String>? = null,
    val keywords: List<String>? = null,
    val roles: List<String>? = null,
    val entity: String? = null,
    val type: String? = null
) {
    init {
        require(name.isNotBlank()) { "Project name must not be blank" }
        require(description.isNotBlank()) { "Project description must not be blank" }

        // Validate URL if provided
        url?.let { if (it.isNotBlank()) Url(it) }

        // If both dates are provided, endDate must be on or after startDate
        if (startDate != null && endDate != null) {
            require(!endDate.isBefore(startDate)) {
                "End date must be on or after start date"
            }
        }
    }
}
