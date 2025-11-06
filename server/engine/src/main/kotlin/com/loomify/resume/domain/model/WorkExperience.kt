package com.loomify.resume.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * CompanyName value object with validation.
 * - Required, max 100 characters
 */
@JvmInline
value class CompanyName(val value: String) {
    init {
        require(value.isNotBlank()) { "Company name cannot be blank" }
        require(value.length <= MAX_COMPANY_LENGTH) { "Company name cannot exceed $MAX_COMPANY_LENGTH characters" }
    }
    companion object {
        private const val MAX_COMPANY_LENGTH = 100
    }
}

/**
 * Highlight value object representing an achievement or responsibility.
 */
@JvmInline
value class Highlight(val value: String) {
    init {
        require(value.isNotBlank()) { "Highlight cannot be blank" }
    }
}

/**
 * Entity representing a work experience entry in a resume.
 * Contains employment history with date validation.
 * Dates are stored as ISO-8601 strings (YYYY-MM-DD).
 */
data class WorkExperience(
    val company: CompanyName,
    val position: JobTitle,
    val startDate: String,
    val endDate: String? = null, // null means current employment
    val location: String? = null,
    val summary: String? = null,
    val highlights: List<Highlight>? = null,
    val url: Url? = null,
) {
    init {
        // Validate date format
        val start = LocalDate.parse(startDate)

        // If endDate is provided, it must be on or after startDate
        endDate?.let {
            val end = LocalDate.parse(it)
            require(!end.isBefore(start)) {
                "End date must be after or equal to start date"
            }
        }
    }

    /**
     * Calculates the duration of employment in years (as Double).
     * Returns fractional years for more accurate duration.
     */
    fun durationInYears(): Double {
        val start = LocalDate.parse(startDate)
        val end = endDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
        val days = ChronoUnit.DAYS.between(start, end)
        return days / DAYS_PER_YEAR // Account for leap years
    }

    /**
     * Formats the employment period for display.
     * Format: "YYYY-MM-DD -- YYYY-MM-DD" or "YYYY-MM-DD -- Present"
     * @param locale Locale object for language-specific formatting
     */
    fun formatPeriod(locale: Locale = Locale.ENGLISH): String {
        val presentLabel = if (locale.language == "es") "Presente" else "Present"
        val end = endDate ?: presentLabel
        return "$startDate -- $end"
    }
    companion object {
        private const val DAYS_PER_YEAR = 365.25
    }
}
