package com.cvix.resume.domain

import java.time.LocalDate
import java.util.*
import java.util.Locale

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
 * Entity representing an education entry in a resume.
 * Dates are stored as ISO-8601 strings (YYYY-MM-DD).
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
