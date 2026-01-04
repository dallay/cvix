package com.cvix.resume.infrastructure.http.request

import com.cvix.resume.infrastructure.http.request.dto.AwardDto
import com.cvix.resume.infrastructure.http.request.dto.BasicsDto
import com.cvix.resume.infrastructure.http.request.dto.CertificateDto
import com.cvix.resume.infrastructure.http.request.dto.EducationDto
import com.cvix.resume.infrastructure.http.request.dto.InterestDto
import com.cvix.resume.infrastructure.http.request.dto.LanguageDto
import com.cvix.resume.infrastructure.http.request.dto.ProjectDto
import com.cvix.resume.infrastructure.http.request.dto.PublicationDto
import com.cvix.resume.infrastructure.http.request.dto.ReferenceDto
import com.cvix.resume.infrastructure.http.request.dto.SkillCategoryDto
import com.cvix.resume.infrastructure.http.request.dto.VolunteerDto
import com.cvix.resume.infrastructure.http.request.dto.WorkExperienceDto
import com.cvix.resume.infrastructure.validation.ValidResumeContent
import jakarta.validation.Valid

/**
 * DTO for resume content following JSON Resume schema.
 * Used for create and update operations (CRUD).
 * Schema reference: https://jsonresume.org/schema/
 * Per FR-001: Must have at least one of work, education, or skills.
 *
 * Note: This differs from GenerateResumeRequest which requires a templateId
 * for PDF generation. This DTO is for pure resume data storage.
 */
@ValidResumeContent
data class ResumeContentRequest(
    @field:Valid
    override val basics: BasicsDto,

    @field:Valid
    override val work: List<WorkExperienceDto>? = null,

    @field:Valid
    override val volunteer: List<VolunteerDto>? = null,

    @field:Valid
    override val education: List<EducationDto>? = null,

    @field:Valid
    override val awards: List<AwardDto>? = null,

    @field:Valid
    override val certificates: List<CertificateDto>? = null,

    @field:Valid
    override val publications: List<PublicationDto>? = null,

    @field:Valid
    override val skills: List<SkillCategoryDto>? = null,

    @field:Valid
    override val languages: List<LanguageDto>? = null,

    @field:Valid
    override val interests: List<InterestDto>? = null,

    @field:Valid
    override val references: List<ReferenceDto>? = null,

    @field:Valid
    override val projects: List<ProjectDto>? = null
) : ResumeDataDto
