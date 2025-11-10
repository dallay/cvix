package com.loomify.resume.domain.model

import java.time.LocalDate
import java.util.Locale
import java.util.ResourceBundle

/**
 * InstitutionName value object with validation.
 * - Required, max 100 characters
 */
@JvmInline
value class InstitutionName(val value: String) {
    init {
        require(value.isNotBlank()) { "Institution name cannot be blank" }
        require(value.length <= MAX_INSTITUTION_LENGTH) {
            "Institution name cannot exceed $MAX_INSTITUTION_LENGTH characters"
        }
    }
    companion object {
        private const val MAX_INSTITUTION_LENGTH = 100
    }
}

/**
 * FieldOfStudy value object with validation.
 * - Required, max 100 characters
 */
@JvmInline
value class FieldOfStudy(val value: String) {
    init {
        require(value.isNotBlank()) { "Field of study cannot be blank" }
        require(value.length <= MAX_FIELD_LENGTH) { "Field of study cannot exceed $MAX_FIELD_LENGTH characters" }
    }
    companion object {
        private const val MAX_FIELD_LENGTH = 100
    }
}

/**
 * DegreeType value object with validation.
 * - Required, max 100 characters
 */
@JvmInline
value class DegreeType(val value: String) {
    init {
        require(value.isNotBlank()) { "Degree type cannot be blank" }
        require(value.length <= MAX_DEGREE_LENGTH) { "Degree type cannot exceed $MAX_DEGREE_LENGTH characters" }
    }
    companion object {
        private const val MAX_DEGREE_LENGTH = 100
    }
}

/**
 * Entity representing an education entry in a resume per JSON Resume Schema.
 * Dates are stored as ISO-8601 strings (YYYY-MM-DD, YYYY-MM, or YYYY).
 */
data class Education(
    val institution: InstitutionName,
    val area: FieldOfStudy? = null,
    val studyType: DegreeType? = null,
    val startDate: String,
    val endDate: String? = null, // null means currently enrolled
    val score: String? = null, // GPA or grade
    val url: Url? = null,
    val courses: List<String>? = null,
) {
    init {
        // Validate date format - supports YYYY-MM-DD, YYYY-MM, or YYYY
        val start = parseFlexibleDate(startDate)
            ?: throw IllegalArgumentException("Invalid start date format: $startDate")

        // If endDate is provided, it must be on or after startDate
        endDate?.let {
            val end = parseFlexibleDate(it)
                ?: throw IllegalArgumentException("Invalid end date format: $it")
            require(!end.isBefore(start)) {
                "End date must be after or equal to start date"
            }
        }
    }

    /**
     * Parses a flexible ISO 8601 date (YYYY-MM-DD, YYYY-MM, or YYYY).
     * Returns null if the format is invalid.
     */
    private fun parseFlexibleDate(date: String): LocalDate? {
        return try {
            when {
                date.matches(Regex("""\d{4}-\d{2}-\d{2}""")) -> LocalDate.parse(date)
                date.matches(Regex("""\d{4}-\d{2}""")) -> LocalDate.parse("$date-01")
                date.matches(Regex("""\d{4}""")) -> LocalDate.parse("$date-01-01")
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Formats the study period for display.
     * Format: "YYYY-MM-DD -- YYYY-MM-DD" or "YYYY-MM-DD -- Present"
     * @param locale Locale object for language-specific formatting
     */
    fun formatPeriod(locale: Locale = Locale.ENGLISH): String {
        val resourceBundle = ResourceBundle.getBundle("messages.messages", locale)
        val presentLabel = resourceBundle.getString("present")
        val end = endDate ?: presentLabel
        return "$startDate -- $end"
    }
}
