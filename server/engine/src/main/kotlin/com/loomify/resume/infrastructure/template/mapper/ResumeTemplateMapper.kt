package com.loomify.resume.infrastructure.template.mapper

import com.loomify.resume.domain.model.Award
import com.loomify.resume.domain.model.Certificate
import com.loomify.resume.domain.model.Education
import com.loomify.resume.domain.model.Interest
import com.loomify.resume.domain.model.Language
import com.loomify.resume.domain.model.PersonalInfo
import com.loomify.resume.domain.model.Project
import com.loomify.resume.domain.model.Publication
import com.loomify.resume.domain.model.Reference
import com.loomify.resume.domain.model.ResumeData
import com.loomify.resume.domain.model.SkillCategory
import com.loomify.resume.domain.model.Volunteer
import com.loomify.resume.domain.model.WorkExperience
import com.loomify.resume.infrastructure.template.model.AwardTemplateModel
import com.loomify.resume.infrastructure.template.model.CertificateTemplateModel
import com.loomify.resume.infrastructure.template.model.EducationTemplateModel
import com.loomify.resume.infrastructure.template.model.InterestTemplateModel
import com.loomify.resume.infrastructure.template.model.LanguageTemplateModel
import com.loomify.resume.infrastructure.template.model.LocationTemplateModel
import com.loomify.resume.infrastructure.template.model.PersonalInfoTemplateModel
import com.loomify.resume.infrastructure.template.model.ProjectTemplateModel
import com.loomify.resume.infrastructure.template.model.PublicationTemplateModel
import com.loomify.resume.infrastructure.template.model.ReferenceTemplateModel
import com.loomify.resume.infrastructure.template.model.ResumeTemplateModel
import com.loomify.resume.infrastructure.template.model.SkillCategoryTemplateModel
import com.loomify.resume.infrastructure.template.model.SocialProfileTemplateModel
import com.loomify.resume.infrastructure.template.model.VolunteerTemplateModel
import com.loomify.resume.infrastructure.template.model.WorkExperienceTemplateModel
import com.loomify.resume.infrastructure.template.util.LatexEscaper

/**
 * Maps domain Resume models to template-friendly models.
 * Unwraps all value classes to simple types for StringTemplate 4.
 */
object ResumeTemplateMapper {

    fun toTemplateModel(resume: ResumeData): ResumeTemplateModel =
        ResumeTemplateModel(
            basics = mapBasics(resume.basics),
            work = resume.work.map { mapWork(it) },
            education = resume.education.map { mapEducation(it) },
            skills = resume.skills.map { mapSkill(it) },
            languages = resume.languages.map { mapLanguage(it) },
            projects = resume.projects.map { mapProject(it) },
            volunteer = resume.volunteer.map { mapVolunteer(it) },
            awards = resume.awards.map { mapAward(it) },
            certificates = resume.certificates.map { mapCertificate(it) },
            publications = resume.publications.map { mapPublication(it) },
            interests = resume.interests.map { mapInterest(it) },
            references = resume.references.map { mapReference(it) },
        )

    private fun mapBasics(basics: PersonalInfo): PersonalInfoTemplateModel =
        PersonalInfoTemplateModel(
            name = LatexEscaper.escape(basics.name.value),
            label = basics.label?.value?.let { LatexEscaper.escape(it) },
            image = basics.image?.value,
            email = LatexEscaper.escape(basics.email.email),
            phone = basics.phone?.value?.let { LatexEscaper.escape(it) },
            url = basics.url?.value, // handled by UrlRenderer if formatted
            summary = basics.summary?.value?.let { LatexEscaper.escape(it) },
            location = basics.location?.let {
                LocationTemplateModel(
                    address = it.address,
                    postalCode = it.postalCode,
                    city = it.city,
                    countryCode = it.countryCode,
                    region = it.region,
                )
            },
            profiles = basics.profiles.map {
                SocialProfileTemplateModel(
                    network = it.network,
                    username = it.username,
                    url = it.url,
                )
            },
        )

    private fun mapWork(work: WorkExperience): WorkExperienceTemplateModel =
        WorkExperienceTemplateModel(
            company = LatexEscaper.escape(work.company.value),
            position = LatexEscaper.escape(work.position.value),
            startDate = work.startDate,
            endDate = work.endDate,
            location = work.location,
            summary = work.summary?.let { LatexEscaper.escape(it) },
            highlights = work.highlights?.map { LatexEscaper.escape(it.value) },
            url = work.url?.value,
        )

    private fun mapEducation(edu: Education): EducationTemplateModel =
        EducationTemplateModel(
            institution = LatexEscaper.escape(edu.institution.value),
            area = edu.area?.value?.let { LatexEscaper.escape(it) },
            studyType = edu.studyType?.value?.let { LatexEscaper.escape(it) },
            startDate = edu.startDate,
            endDate = edu.endDate,
            score = edu.score,
            url = edu.url?.value,
            courses = edu.courses?.map { LatexEscaper.escape(it) },
        )

    private fun mapSkill(skill: SkillCategory): SkillCategoryTemplateModel =
        SkillCategoryTemplateModel(
            name = LatexEscaper.escape(skill.name.value),
            level = skill.level,
            keywords = skill.keywords.map { LatexEscaper.escape(it.value) },
        )

    private fun mapProject(project: Project): ProjectTemplateModel =
        ProjectTemplateModel(
            name = LatexEscaper.escape(project.name),
            description = LatexEscaper.escape(project.description),
            url = project.url,
            startDate = project.startDate?.toString(),
            endDate = project.endDate?.toString(),
            highlights = project.highlights?.map { LatexEscaper.escape(it) },
            keywords = project.keywords?.map { LatexEscaper.escape(it) },
            roles = project.roles?.map { LatexEscaper.escape(it) },
            entity = project.entity?.let { LatexEscaper.escape(it) },
            type = project.type?.let { LatexEscaper.escape(it) },
        )

    private fun mapPublication(pub: Publication): PublicationTemplateModel =
        PublicationTemplateModel(
            name = LatexEscaper.escape(pub.name),
            publisher = pub.publisher?.let { LatexEscaper.escape(it) } ?: "",
            releaseDate = pub.releaseDate ?: "",
            url = pub.url?.value,
            summary = pub.summary?.let { LatexEscaper.escape(it) },
        )

    private fun mapCertificate(cert: Certificate): CertificateTemplateModel =
        CertificateTemplateModel(
            name = LatexEscaper.escape(cert.name),
            date = cert.date ?: "",
            issuer = cert.issuer?.let { LatexEscaper.escape(it) } ?: "",
            url = cert.url?.value,
        )

    private fun mapAward(award: Award): AwardTemplateModel =
        AwardTemplateModel(
            title = LatexEscaper.escape(award.title),
            date = award.date ?: "",
            awarder = award.awarder?.let { LatexEscaper.escape(it) } ?: "",
            summary = award.summary?.let { LatexEscaper.escape(it) },
        )

    private fun mapVolunteer(vol: Volunteer): VolunteerTemplateModel =
        VolunteerTemplateModel(
            organization = LatexEscaper.escape(vol.organization),
            position = LatexEscaper.escape(vol.position),
            url = vol.url?.value,
            startDate = vol.startDate ?: "",
            endDate = vol.endDate,
            summary = vol.summary?.let { LatexEscaper.escape(it) },
            highlights = vol.highlights?.map { LatexEscaper.escape(it) },
        )

    private fun mapLanguage(lang: Language): LanguageTemplateModel =
        LanguageTemplateModel(
            language = LatexEscaper.escape(lang.language),
            fluency = LatexEscaper.escape(lang.fluency),
        )

    private fun mapInterest(interest: Interest): InterestTemplateModel =
        InterestTemplateModel(
            name = LatexEscaper.escape(interest.name),
            keywords = interest.keywords?.map { LatexEscaper.escape(it) } ?: emptyList(),
        )

    private fun mapReference(ref: Reference): ReferenceTemplateModel =
        ReferenceTemplateModel(
            name = LatexEscaper.escape(ref.name),
            reference = ref.reference?.let { LatexEscaper.escape(it) } ?: "",
        )
}
