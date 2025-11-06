package com.loomify.resume.infrastructure.web.dto

import com.loomify.resume.infrastructure.validation.ValidResumeContent
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * DTO for resume generation request.
 * Per FR-001: Must have at least one of work, education, or skills.
 */
@ValidResumeContent
data class GenerateResumeRequest(
    @field:Valid
    val personalInfo: PersonalInfoDto,

    @field:Valid
    val workExperience: List<WorkExperienceDto>? = null,

    @field:Valid
    val education: List<EducationDto>? = null,

    @field:Valid
    val skills: List<SkillCategoryDto>? = null,

    @field:Valid
    val languages: List<LanguageDto>? = null,

    @field:Valid
    val projects: List<ProjectDto>? = null
)

/**
 * DTO for personal information.
 */
data class PersonalInfoDto(
    @field:NotBlank(message = "Full name is required")
    @field:Size(max = 100, message = "Full name must not exceed 100 characters")
    val fullName: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Phone is required")
    val phone: String,

    @field:Size(max = 200, message = "Location must not exceed 200 characters")
    val location: String? = null,

    val linkedin: String? = null,
    val github: String? = null,
    val website: String? = null,

    @field:Size(max = 500, message = "Summary must not exceed 500 characters")
    val summary: String? = null
)

/**
 * DTO for work experience.
 */
data class WorkExperienceDto(
    @field:NotBlank(message = "Company is required")
    @field:Size(max = 100, message = "Company must not exceed 100 characters")
    val company: String,

    @field:NotBlank(message = "Position is required")
    @field:Size(max = 100, message = "Position must not exceed 100 characters")
    val position: String,

    @field:NotBlank(message = "Start date is required")
    val startDate: String, // ISO date format: YYYY-MM-DD

    val endDate: String? = null, // ISO date format: YYYY-MM-DD

    @field:Size(max = 200, message = "Location must not exceed 200 characters")
    val location: String? = null,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String? = null
)

/**
 * DTO for education.
 */
data class EducationDto(
    @field:NotBlank(message = "Institution is required")
    @field:Size(max = 100, message = "Institution must not exceed 100 characters")
    val institution: String,

    @field:NotBlank(message = "Degree is required")
    @field:Size(max = 100, message = "Degree must not exceed 100 characters")
    val degree: String,

    @field:NotBlank(message = "Start date is required")
    val startDate: String, // ISO date format: YYYY-MM-DD

    val endDate: String? = null, // ISO date format: YYYY-MM-DD

    @field:Size(max = 200, message = "Location must not exceed 200 characters")
    val location: String? = null,

    @field:Size(max = 10, message = "GPA must not exceed 10 characters")
    val gpa: String? = null,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String? = null
)

/**
 * DTO for skill category.
 */
data class SkillCategoryDto(
    @field:NotBlank(message = "Skill category name is required")
    @field:Size(max = 100, message = "Skill category name must not exceed 100 characters")
    val name: String,

    @field:Size(min = 1, message = "At least one skill is required")
    val keywords: List<
        @Size(max = 50, message = "Skill must not exceed 50 characters")
        String,
        >
)

/**
 * DTO for language proficiency.
 */
data class LanguageDto(
    @field:NotBlank(message = "Language is required")
    @field:Size(max = 50, message = "Language must not exceed 50 characters")
    val language: String,

    @field:NotBlank(message = "Fluency is required")
    @field:Size(max = 50, message = "Fluency must not exceed 50 characters")
    val fluency: String
)

/**
 * DTO for project.
 */
data class ProjectDto(
    @field:NotBlank(message = "Project name is required")
    @field:Size(max = 100, message = "Project name must not exceed 100 characters")
    val name: String,

    @field:NotBlank(message = "Project description is required")
    @field:Size(max = 500, message = "Project description must not exceed 500 characters")
    val description: String,

    val url: String? = null,

    @field:Pattern(
        regexp = """^\d{4}-\d{2}-\d{2}$""",
        message = "startDate must be ISO yyyy-MM-dd",
    )
    val startDate: String? = null, // ISO date format: YYYY-MM-DD

    @field:Pattern(
        regexp = """^\d{4}-\d{2}-\d{2}$""",
        message = "endDate must be ISO yyyy-MM-dd",
    )
    val endDate: String? = null // ISO date format: YYYY-MM-DD
)
