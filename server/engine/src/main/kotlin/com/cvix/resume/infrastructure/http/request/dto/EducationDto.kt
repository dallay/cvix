package com.cvix.resume.infrastructure.http.request.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for education.
 */
data class EducationDto(
    @field:NotBlank(message = "Institution is required")
    @field:Size(max = 100, message = "Institution must not exceed 100 characters")
    val institution: String,

    @field:Size(max = 100, message = "Area must not exceed 100 characters")
    val area: String? = null,

    @field:Size(max = 100, message = "Study type must not exceed 100 characters")
    val studyType: String? = null,

    @field:NotBlank(message = "Start date is required")
    val startDate: String, // ISO date format: YYYY-MM-DD

    val endDate: String? = null, // ISO date format: YYYY-MM-DD

    @field:Size(max = 10, message = "Score must not exceed 10 characters")
    val score: String? = null,

    val url: String? = null,

    val courses: List<
        @Size(max = 200, message = "Course must not exceed 200 characters")
        String,
        >? = null
)
