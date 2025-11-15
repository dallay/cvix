package com.loomify.resume.infrastructure.template.model

/**
 * Template-friendly model for PersonalInfo.
 * All properties are exposed as simple types for StringTemplate 4 access.
 */
data class PersonalInfoTemplateModel(
    val name: String,
    val label: String?,
    val image: String?,
    val email: String,
    val phone: String?,
    val url: String?,
    val summary: String?,
    val location: LocationTemplateModel?,
    val profiles: List<SocialProfileTemplateModel>,
)

data class LocationTemplateModel(
    val address: String?,
    val postalCode: String?,
    val city: String?,
    val countryCode: String?,
    val region: String?,
)

data class SocialProfileTemplateModel(
    val network: String,
    val username: String,
    val url: String,
)

data class WorkExperienceTemplateModel(
    val company: String,
    val position: String,
    val startDate: String,
    val endDate: String?,
    val location: String?,
    val summary: String?,
    val highlights: List<String>?,
    val url: String?,
)

data class EducationTemplateModel(
    val institution: String,
    val area: String?,
    val studyType: String?,
    val startDate: String,
    val endDate: String?,
    val score: String?,
    val url: String?,
    val courses: List<String>?,
)

data class SkillCategoryTemplateModel(
    val name: String,
    val level: String?,
    val keywords: List<String>,
)

data class ProjectTemplateModel(
    val name: String,
    val description: String,
    val url: String?,
    val startDate: String?,
    val endDate: String?,
    val highlights: List<String>?,
    val keywords: List<String>?,
    val roles: List<String>?,
    val entity: String?,
    val type: String?,
)

data class PublicationTemplateModel(
    val name: String,
    val publisher: String,
    val releaseDate: String,
    val url: String?,
    val summary: String?,
)

data class CertificateTemplateModel(
    val name: String,
    val date: String,
    val issuer: String,
    val url: String?,
)

data class AwardTemplateModel(
    val title: String,
    val date: String,
    val awarder: String,
    val summary: String?,
)

data class VolunteerTemplateModel(
    val organization: String,
    val position: String,
    val url: String?,
    val startDate: String,
    val endDate: String?,
    val summary: String?,
    val highlights: List<String>?,
)

data class LanguageTemplateModel(
    val language: String,
    val fluency: String,
)

data class InterestTemplateModel(
    val name: String,
    val keywords: List<String>,
)

data class ReferenceTemplateModel(
    val name: String,
    val reference: String,
)

/**
 * Main template model for resume rendering.
 * All nested value classes are unwrapped to simple types.
 */
data class ResumeTemplateModel(
    val basics: PersonalInfoTemplateModel,
    val work: List<WorkExperienceTemplateModel>,
    val education: List<EducationTemplateModel>,
    val skills: List<SkillCategoryTemplateModel>,
    val languages: List<LanguageTemplateModel>,
    val projects: List<ProjectTemplateModel>,
    val volunteer: List<VolunteerTemplateModel>,
    val awards: List<AwardTemplateModel>,
    val certificates: List<CertificateTemplateModel>,
    val publications: List<PublicationTemplateModel>,
    val interests: List<InterestTemplateModel>,
    val references: List<ReferenceTemplateModel>,
)
