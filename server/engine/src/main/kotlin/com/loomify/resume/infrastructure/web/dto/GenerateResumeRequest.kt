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
 * Supports JSON Resume Schema with all optional sections.
 */
@ValidResumeContent
data class GenerateResumeRequest(
    @field:Valid
    val personalInfo: PersonalInfoDto,

    @field:Valid
    val workExperience: List<WorkExperienceDto>? = null,

    @field:Valid
    val volunteer: List<VolunteerDto>? = null,

    @field:Valid
    val education: List<EducationDto>? = null,

    @field:Valid
    val awards: List<AwardDto>? = null,

    @field:Valid
    val certificates: List<CertificateDto>? = null,

    @field:Valid
    val publications: List<PublicationDto>? = null,

    @field:Valid
    val skills: List<SkillCategoryDto>? = null,

    @field:Valid
    val languages: List<LanguageDto>? = null,

    @field:Valid
    val interests: List<InterestDto>? = null,

    @field:Valid
    val references: List<ReferenceDto>? = null,

    @field:Valid
    val projects: List<ProjectDto>? = null,

    @field:Valid
    val meta: MetaDto? = null
)

/**
 * DTO for location information per JSON Resume Schema.
 */
data class LocationDto(
    val address: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    val countryCode: String? = null,
    val region: String? = null
)

/**
 * DTO for social network profile per JSON Resume Schema.
 */
data class ProfileDto(
    @field:Size(max = 50, message = "Network must not exceed 50 characters")
    val network: String,

    @field:Size(max = 50, message = "Username must not exceed 50 characters")
    val username: String? = null,

    @field:Size(max = 200, message = "URL must not exceed 200 characters")
    val url: String
)

/**
 * DTO for personal information.
 */
data class PersonalInfoDto(
    @field:NotBlank(message = "Full name is required")
    @field:Size(max = 100, message = "Full name must not exceed 100 characters")
    val fullName: String,

    @field:Size(max = 100, message = "Label must not exceed 100 characters")
    val label: String? = null,

    @field:Size(max = 200, message = "Image URL must not exceed 200 characters")
    val image: String? = null,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Phone is required")
    val phone: String,

    @field:Size(max = 200, message = "URL must not exceed 200 characters")
    val url: String? = null,

    @field:Size(max = 500, message = "Summary must not exceed 500 characters")
    val summary: String? = null,

    @field:Valid
    val location: LocationDto? = null,

    @field:Valid
    val profiles: List<ProfileDto>? = null,

    // Legacy fields for backward compatibility
    @Deprecated("Use profiles instead")
    val linkedin: String? = null,

    @Deprecated("Use profiles instead")
    val github: String? = null,

    @Deprecated("Use url instead")
    val website: String? = null
)

/**
 * DTO for work experience per JSON Resume Schema.
 */
data class WorkExperienceDto(
    @field:NotBlank(message = "Company name is required")
    @field:Size(max = 100, message = "Company name must not exceed 100 characters")
    val name: String,

    @field:NotBlank(message = "Position is required")
    @field:Size(max = 100, message = "Position must not exceed 100 characters")
    val position: String,

    @field:NotBlank(message = "Start date is required")
    val startDate: String, // ISO date format: YYYY-MM-DD or YYYY-MM or YYYY

    val endDate: String? = null, // ISO date format: YYYY-MM-DD or YYYY-MM or YYYY

    @field:Size(max = 200, message = "Location must not exceed 200 characters")
    val location: String? = null,

    @field:Size(max = 500, message = "Summary must not exceed 500 characters")
    val summary: String? = null,

    @field:Size(max = 200, message = "URL must not exceed 200 characters")
    val url: String? = null,

    val highlights: List<
        @Size(max = 500, message = "Highlight must not exceed 500 characters")
        String
    >? = null,

    // Legacy field for backward compatibility
    @Deprecated("Use name instead")
    val company: String? = null,

    @Deprecated("Use summary instead")
    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String? = null
)

/**
 * DTO for education per JSON Resume Schema.
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
    val startDate: String, // ISO date format: YYYY-MM-DD or YYYY-MM or YYYY

    val endDate: String? = null, // ISO date format: YYYY-MM-DD or YYYY-MM or YYYY

    @field:Size(max = 20, message = "Score must not exceed 20 characters")
    val score: String? = null,

    @field:Size(max = 200, message = "URL must not exceed 200 characters")
    val url: String? = null,

    val courses: List<
        @Size(max = 100, message = "Course must not exceed 100 characters")
        String
    >? = null,

    // Legacy fields for backward compatibility
    @Deprecated("Use area and studyType instead")
    @field:Size(max = 100, message = "Degree must not exceed 100 characters")
    val degree: String? = null,

    @Deprecated("Not part of JSON Resume Schema")
    @field:Size(max = 200, message = "Location must not exceed 200 characters")
    val location: String? = null,

    @Deprecated("Use score instead")
    @field:Size(max = 10, message = "GPA must not exceed 10 characters")
    val gpa: String? = null,

    @Deprecated("Not part of JSON Resume Schema")
    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String? = null
)

/**
 * DTO for skill category per JSON Resume Schema.
 */
data class SkillCategoryDto(
    @field:NotBlank(message = "Skill category name is required")
    @field:Size(max = 100, message = "Skill category name must not exceed 100 characters")
    val name: String,

    @field:Size(max = 50, message = "Level must not exceed 50 characters")
    val level: String? = null,

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
 * DTO for project per JSON Resume Schema.
 */
data class ProjectDto(
    @field:NotBlank(message = "Project name is required")
    @field:Size(max = 100, message = "Project name must not exceed 100 characters")
    val name: String,

    @field:NotBlank(message = "Project description is required")
    @field:Size(max = 500, message = "Project description must not exceed 500 characters")
    val description: String,

    @field:Size(max = 200, message = "URL must not exceed 200 characters")
    val url: String? = null,

    @field:Pattern(
        regexp = """^(\d{4}-\d{2}-\d{2}|\d{4}-\d{2}|\d{4})$""",
        message = "startDate must be ISO format (YYYY-MM-DD, YYYY-MM, or YYYY)",
    )
    val startDate: String? = null,

    @field:Pattern(
        regexp = """^(\d{4}-\d{2}-\d{2}|\d{4}-\d{2}|\d{4})$""",
        message = "endDate must be ISO format (YYYY-MM-DD, YYYY-MM, or YYYY)",
    )
    val endDate: String? = null,

    val highlights: List<
        @Size(max = 500, message = "Highlight must not exceed 500 characters")
        String
    >? = null,

    val keywords: List<
        @Size(max = 50, message = "Keyword must not exceed 50 characters")
        String
    >? = null,

    val roles: List<
        @Size(max = 100, message = "Role must not exceed 100 characters")
        String
    >? = null,

    @field:Size(max = 100, message = "Entity must not exceed 100 characters")
    val entity: String? = null,

    @field:Size(max = 50, message = "Type must not exceed 50 characters")
    val type: String? = null
)

/**
 * DTO for volunteer work per JSON Resume Schema.
 */
data class VolunteerDto(
    @field:NotBlank(message = "Organization is required")
    @field:Size(max = 100, message = "Organization must not exceed 100 characters")
    val organization: String,

    @field:NotBlank(message = "Position is required")
    @field:Size(max = 100, message = "Position must not exceed 100 characters")
    val position: String,

    @field:Size(max = 200, message = "URL must not exceed 200 characters")
    val url: String? = null,

    val startDate: String? = null,

    val endDate: String? = null,

    @field:Size(max = 500, message = "Summary must not exceed 500 characters")
    val summary: String? = null,

    val highlights: List<
        @Size(max = 500, message = "Highlight must not exceed 500 characters")
        String
    >? = null
)

/**
 * DTO for award per JSON Resume Schema.
 */
data class AwardDto(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 100, message = "Title must not exceed 100 characters")
    val title: String,

    val date: String? = null,

    @field:Size(max = 100, message = "Awarder must not exceed 100 characters")
    val awarder: String? = null,

    @field:Size(max = 500, message = "Summary must not exceed 500 characters")
    val summary: String? = null
)

/**
 * DTO for certificate per JSON Resume Schema.
 */
data class CertificateDto(
    @field:NotBlank(message = "Certificate name is required")
    @field:Size(max = 100, message = "Name must not exceed 100 characters")
    val name: String,

    val date: String? = null,

    @field:Size(max = 200, message = "URL must not exceed 200 characters")
    val url: String? = null,

    @field:Size(max = 100, message = "Issuer must not exceed 100 characters")
    val issuer: String? = null
)

/**
 * DTO for publication per JSON Resume Schema.
 */
data class PublicationDto(
    @field:NotBlank(message = "Publication name is required")
    @field:Size(max = 200, message = "Name must not exceed 200 characters")
    val name: String,

    @field:Size(max = 100, message = "Publisher must not exceed 100 characters")
    val publisher: String? = null,

    val releaseDate: String? = null,

    @field:Size(max = 200, message = "URL must not exceed 200 characters")
    val url: String? = null,

    @field:Size(max = 500, message = "Summary must not exceed 500 characters")
    val summary: String? = null
)

/**
 * DTO for interest per JSON Resume Schema.
 */
data class InterestDto(
    @field:NotBlank(message = "Interest name is required")
    @field:Size(max = 100, message = "Name must not exceed 100 characters")
    val name: String,

    val keywords: List<
        @Size(max = 50, message = "Keyword must not exceed 50 characters")
        String
    >? = null
)

/**
 * DTO for reference per JSON Resume Schema.
 */
data class ReferenceDto(
    @field:NotBlank(message = "Reference name is required")
    @field:Size(max = 100, message = "Name must not exceed 100 characters")
    val name: String,

    @field:Size(max = 500, message = "Reference text must not exceed 500 characters")
    val reference: String? = null
)

/**
 * DTO for schema metadata per JSON Resume Schema.
 */
data class MetaDto(
    @field:Size(max = 200, message = "Canonical URL must not exceed 200 characters")
    val canonical: String? = null,

    @field:Size(max = 20, message = "Version must not exceed 20 characters")
    val version: String? = null,

    val lastModified: String? = null
)
