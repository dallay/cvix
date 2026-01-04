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
import jakarta.validation.Valid

/**
 * Shared resume data structure following JSON Resume schema.
 * This interface eliminates duplication between GenerateResumeRequest and ResumeContentRequest.
 * Schema reference: https://jsonresume.org/schema/
 *
 * Note: Complexity is inherent to JSON Resume schema specification.
 * The 12 properties reflect the official resume data model and cannot be simplified
 * without breaking compatibility with the standard.
 */
@Suppress("ComplexInterface")
interface ResumeDataDto {
    val basics: BasicsDto

    @get:Valid
    val work: List<WorkExperienceDto>?

    @get:Valid
    val volunteer: List<VolunteerDto>?

    @get:Valid
    val education: List<EducationDto>?

    @get:Valid
    val awards: List<AwardDto>?

    @get:Valid
    val certificates: List<CertificateDto>?

    @get:Valid
    val publications: List<PublicationDto>?

    @get:Valid
    val skills: List<SkillCategoryDto>?

    @get:Valid
    val languages: List<LanguageDto>?

    @get:Valid
    val interests: List<InterestDto>?

    @get:Valid
    val references: List<ReferenceDto>?

    @get:Valid
    val projects: List<ProjectDto>?
}
