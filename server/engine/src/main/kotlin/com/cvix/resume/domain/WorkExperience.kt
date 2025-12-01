package com.cvix.resume.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
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
 * Work experience entry representing a single employment record in a resume.
 *
 * ## Date Handling
 * - Dates stored as ISO-8601 strings (YYYY-MM-DD), parsed via [LocalDate]
 * - `endDate = null` signals ongoing employment (current role)
 * - Enforces chronological integrity: end date ≥ start date
 *
 * ## Validation Rules
 * - `startDate`: Must parse as valid ISO-8601 date
 * - `endDate`: If present, must parse and be ≥ `startDate`
 * - Throws [IllegalArgumentException] on chronology violations
 * - Throws [java.time.format.DateTimeParseException] on malformed date strings
 *
 * ## Example JSON Payload
 * ```json
 * {
 *   "name": "Acme Corp",
 *   "position": "Senior Engineer",
 *   "url": "https://acme.com",
 *   "startDate": "2020-03-15",
 *   "endDate": "2023-06-30",
 *   "location": "San Francisco, CA",
 *   "summary": "Led platform migration to microservices architecture",
 *   "highlights": [
 *     "Reduced deployment time by 60%",
 *     "Mentored team of 5 engineers"
 *   ]
 * }
 * ```
 *
 * @property name Validated company name (max 100 chars, non-empty)
 * @property position Job title value object with validation
 * @property startDate ISO-8601 employment start date (required)
 * @property endDate ISO-8601 employment end date (null = current role)
 * @property location Geographic location string (optional)
 * @property summary Concise role description (optional)
 * @property highlights Key achievements list (optional, validated)
 * @property url Company website [Url] value object (optional)
 *
 * @throws IllegalArgumentException If end date precedes start date
 * @throws java.time.format.DateTimeParseException If date strings aren't valid ISO-8601 format
 */
data class WorkExperience(
    val name: CompanyName,
    val position: JobTitle,
    val startDate: String,
    val endDate: String? = null,
    val location: String? = null,
    val summary: String? = null,
    val highlights: List<Highlight>? = null,
    val url: Url? = null,
) {
    init {
        val start = LocalDate.parse(startDate)

        endDate?.let {
            val end = LocalDate.parse(it)
            require(!end.isBefore(start)) {
                "End date ($it) must be on or after start date ($startDate)"
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
        val resourceBundle = ResourceBundle.getBundle("messages.messages", locale)
        val presentLabel = resourceBundle.getString("present")
        val end = endDate ?: presentLabel
        return "$startDate -- $end"
    }

    companion object {
        private const val DAYS_PER_YEAR = 365.25
    }
}
