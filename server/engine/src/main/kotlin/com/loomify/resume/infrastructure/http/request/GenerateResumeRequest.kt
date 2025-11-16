package com.loomify.resume.infrastructure.http.request

import com.loomify.resume.infrastructure.http.request.dto.AwardDto
import com.loomify.resume.infrastructure.http.request.dto.CertificateDto
import com.loomify.resume.infrastructure.http.request.dto.EducationDto
import com.loomify.resume.infrastructure.http.request.dto.InterestDto
import com.loomify.resume.infrastructure.http.request.dto.LanguageDto
import com.loomify.resume.infrastructure.http.request.dto.PersonalInfoDto
import com.loomify.resume.infrastructure.http.request.dto.ProjectDto
import com.loomify.resume.infrastructure.http.request.dto.PublicationDto
import com.loomify.resume.infrastructure.http.request.dto.ReferenceDto
import com.loomify.resume.infrastructure.http.request.dto.SkillCategoryDto
import com.loomify.resume.infrastructure.http.request.dto.VolunteerDto
import com.loomify.resume.infrastructure.http.request.dto.WorkExperienceDto
import com.loomify.resume.infrastructure.validation.ValidResumeContent
import jakarta.validation.Valid

/**
 * DTO for resume generation request following JSON Resume schema.
 * Schema reference: https://jsonresume.org/schema/
 * Per FR-001: Must have at least one of work, education, or skills.
 */
@ValidResumeContent
data class GenerateResumeRequest(
    @field:Valid
    val basics: PersonalInfoDto,

    @field:Valid
    val work: List<WorkExperienceDto>? = null,

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
    val projects: List<ProjectDto>? = null
)
