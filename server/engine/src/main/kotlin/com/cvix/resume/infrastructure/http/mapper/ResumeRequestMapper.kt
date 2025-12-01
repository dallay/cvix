package com.cvix.resume.infrastructure.http.mapper

import com.cvix.common.domain.vo.email.Email
import com.cvix.resume.domain.Award
import com.cvix.resume.domain.Basics
import com.cvix.resume.domain.Certificate
import com.cvix.resume.domain.CompanyName
import com.cvix.resume.domain.DegreeType
import com.cvix.resume.domain.Education
import com.cvix.resume.domain.FieldOfStudy
import com.cvix.resume.domain.FullName
import com.cvix.resume.domain.Highlight
import com.cvix.resume.domain.InstitutionName
import com.cvix.resume.domain.Interest
import com.cvix.resume.domain.JobTitle
import com.cvix.resume.domain.Language
import com.cvix.resume.domain.Location
import com.cvix.resume.domain.PhoneNumber
import com.cvix.resume.domain.Project
import com.cvix.resume.domain.Publication
import com.cvix.resume.domain.Reference
import com.cvix.resume.domain.Resume
import com.cvix.resume.domain.Skill
import com.cvix.resume.domain.SkillCategory
import com.cvix.resume.domain.SkillCategoryName
import com.cvix.resume.domain.SocialProfile
import com.cvix.resume.domain.Summary
import com.cvix.resume.domain.Url
import com.cvix.resume.domain.Volunteer
import com.cvix.resume.domain.WorkExperience
import com.cvix.resume.infrastructure.http.request.GenerateResumeRequest
import com.cvix.resume.infrastructure.http.request.dto.AwardDto
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
import java.time.LocalDate

object ResumeRequestMapper {
    fun toDomain(request: GenerateResumeRequest): Resume {
        return Resume(
            basics = mapBasics(request),
            work = request.work?.map { mapWork(it) } ?: emptyList(),
            education = request.education?.map { mapEducation(it) } ?: emptyList(),
            skills = request.skills?.map { mapSkill(it) } ?: emptyList(),
            languages = request.languages?.map { mapLanguage(it) } ?: emptyList(),
            projects = request.projects?.map { mapProject(it) } ?: emptyList(),
            volunteer = request.volunteer?.map { mapVolunteer(it) } ?: emptyList(),
            awards = request.awards?.map { mapAward(it) } ?: emptyList(),
            certificates = request.certificates?.map { mapCertificate(it) } ?: emptyList(),
            publications = request.publications?.map { mapPublication(it) } ?: emptyList(),
            interests = request.interests?.map { mapInterest(it) } ?: emptyList(),
            references = request.references?.map { mapReference(it) } ?: emptyList(),
        )
    }

    private fun mapBasics(request: GenerateResumeRequest): Basics {
        val basics = request.basics
        return Basics(
            name = FullName(basics.name),
            label = basics.label?.let { JobTitle(it) },
            image = basics.image?.takeIf { it.isNotBlank() }?.let { Url(it) },
            email = Email(basics.email),
            phone = PhoneNumber(basics.phone),
            url = basics.url?.takeIf { it.isNotBlank() }?.let { Url(it) },
            summary = basics.summary?.let { Summary(it) },
            location = basics.location?.let {
                Location(
                    address = it.address,
                    postalCode = it.postalCode,
                    city = it.city,
                    countryCode = it.countryCode,
                    region = it.region,
                )
            },
            profiles = basics.profiles?.map { profile ->
                SocialProfile(profile.network, profile.username ?: "", profile.url)
            } ?: emptyList(),
        )
    }

    private fun mapWork(work: WorkExperienceDto): WorkExperience =
        WorkExperience(
            name = CompanyName(work.name),
            position = JobTitle(work.position),
            startDate = work.startDate,
            endDate = work.endDate,
            location = work.location,
            summary = work.summary ?: work.description,
            highlights = work.highlights?.map { Highlight(it) },
            url = work.url?.takeIf { it.isNotBlank() }?.let { Url(it) },
        )

    private fun mapEducation(edu: EducationDto): Education =
        Education(
            institution = InstitutionName(edu.institution),
            area = edu.area?.let { FieldOfStudy(it) },
            studyType = edu.studyType?.let { DegreeType(it) },
            startDate = edu.startDate,
            endDate = edu.endDate,
            score = edu.score,
            url = edu.url?.takeIf { it.isNotBlank() }?.let { Url(it) },
            courses = edu.courses,
        )

    private fun mapSkill(skill: SkillCategoryDto): SkillCategory =
        SkillCategory(
            name = SkillCategoryName(skill.name),
            level = skill.level,
            keywords = skill.keywords.map { Skill(it) },
        )

    private fun mapLanguage(lang: LanguageDto): Language =
        Language(
            language = lang.language,
            fluency = lang.fluency,
        )

    private fun mapProject(project: ProjectDto): Project =
        Project(
            name = project.name,
            description = project.description,
            url = project.url,
            startDate = project.startDate?.let { LocalDate.parse(it) },
            endDate = project.endDate?.let { LocalDate.parse(it) },
            highlights = project.highlights,
            keywords = project.keywords,
            roles = project.roles,
            entity = project.entity,
            type = project.type,
        )

    private fun mapVolunteer(vol: VolunteerDto): Volunteer =
        Volunteer(
            organization = vol.organization,
            position = vol.position,
            url = vol.url?.takeIf { it.isNotBlank() }?.let { Url(it) },
            startDate = vol.startDate,
            endDate = vol.endDate,
            summary = vol.summary,
            highlights = vol.highlights,
        )

    private fun mapAward(award: AwardDto): Award =
        Award(
            title = award.title,
            date = award.date,
            awarder = award.awarder,
            summary = award.summary,
        )

    private fun mapCertificate(cert: CertificateDto): Certificate =
        Certificate(
            name = cert.name,
            date = cert.date,
            url = cert.url?.takeIf { it.isNotBlank() }?.let { Url(it) },
            issuer = cert.issuer,
        )

    private fun mapPublication(pub: PublicationDto): Publication =
        Publication(
            name = pub.name,
            publisher = pub.publisher,
            releaseDate = pub.releaseDate,
            url = pub.url?.takeIf { it.isNotBlank() }?.let { Url(it) },
            summary = pub.summary,
        )

    private fun mapInterest(interest: InterestDto): Interest =
        Interest(
            name = interest.name,
            keywords = interest.keywords,
        )

    private fun mapReference(ref: ReferenceDto): Reference =
        Reference(
            name = ref.name,
            reference = ref.reference,
        )
}
