package com.cvix.resume.infrastructure.template.mapper

import com.cvix.resume.domain.Award
import com.cvix.resume.domain.Basics
import com.cvix.resume.domain.Certificate
import com.cvix.resume.domain.Education
import com.cvix.resume.domain.Interest
import com.cvix.resume.domain.Language
import com.cvix.resume.domain.Project
import com.cvix.resume.domain.Publication
import com.cvix.resume.domain.Reference
import com.cvix.resume.domain.Resume
import com.cvix.resume.domain.SkillCategory
import com.cvix.resume.domain.Volunteer
import com.cvix.resume.domain.WorkExperience
import com.cvix.resume.infrastructure.template.model.AwardTemplateModel
import com.cvix.resume.infrastructure.template.model.BasicsTemplateModel
import com.cvix.resume.infrastructure.template.model.CertificateTemplateModel
import com.cvix.resume.infrastructure.template.model.EducationTemplateModel
import com.cvix.resume.infrastructure.template.model.InterestTemplateModel
import com.cvix.resume.infrastructure.template.model.LanguageTemplateModel
import com.cvix.resume.infrastructure.template.model.LocationTemplateModel
import com.cvix.resume.infrastructure.template.model.ProjectTemplateModel
import com.cvix.resume.infrastructure.template.model.PublicationTemplateModel
import com.cvix.resume.infrastructure.template.model.ReferenceTemplateModel
import com.cvix.resume.infrastructure.template.model.ResumeTemplateModel
import com.cvix.resume.infrastructure.template.model.SkillCategoryTemplateModel
import com.cvix.resume.infrastructure.template.model.SocialProfileTemplateModel
import com.cvix.resume.infrastructure.template.model.VolunteerTemplateModel
import com.cvix.resume.infrastructure.template.model.WorkExperienceTemplateModel
import com.cvix.resume.infrastructure.template.util.LatexEscaper

/**
 * Maps domain Resume models to template-friendly models.
 * Unwraps all value classes to simple types for StringTemplate 4.
 */
object ResumeTemplateMapper {

    fun toTemplateModel(resume: Resume): ResumeTemplateModel =
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

    private fun mapBasics(basics: Basics): BasicsTemplateModel =
        BasicsTemplateModel(
            name = LatexEscaper.escape(basics.name.value),
            label = basics.label?.value?.let { LatexEscaper.escape(it) },
            image = basics.image?.value,
            email = LatexEscaper.escape(basics.email.value),
            phone = basics.phone?.value?.let { LatexEscaper.escape(it) },
            url = basics.url?.value, // handled by UrlRenderer if formatted
            summary = basics.summary?.value?.let { LatexEscaper.escape(it) },
            location = basics.location?.let {
                LocationTemplateModel(
                    address = it.address?.let(LatexEscaper::escape),
                    postalCode = it.postalCode?.let(LatexEscaper::escape),
                    city = it.city?.let(LatexEscaper::escape),
                    countryCode = it.countryCode?.let(LatexEscaper::escape),
                    region = it.region?.let(LatexEscaper::escape),
                )
            },
            profiles = basics.profiles.map {
                SocialProfileTemplateModel(
                    network = LatexEscaper.escape(it.network),
                    username = LatexEscaper.escape(it.username),
                    url = it.url,
                )
            },
        )

    /**
     * Converts a WorkExperience into a template-friendly WorkExperienceTemplateModel.
     *
     * Textual fields are escaped for LaTeX; optional fields remain null when absent.
     *
     * @return A WorkExperienceTemplateModel where name, position, location, summary, and highlights
     * are LaTeX-escaped, `endDate` is set to null when the source is blank, and `url` is passed through as provided.
     */
    private fun mapWork(work: WorkExperience): WorkExperienceTemplateModel =
        WorkExperienceTemplateModel(
            name = LatexEscaper.escape(work.name.value),
            position = LatexEscaper.escape(work.position.value),
            startDate = work.startDate,
            endDate = work.endDate?.takeIf { it.isNotBlank() }, // Normalize empty strings to null
            location = work.location?.let(LatexEscaper::escape),
            summary = work.summary?.let { LatexEscaper.escape(it) },
            highlights = work.highlights?.map { LatexEscaper.escape(it.value) },
            url = work.url?.value,
        )

    /**
     * Converts an Education domain model into an EducationTemplateModel for template rendering,
     * unwrapping value classes, escaping user-visible text for LaTeX, and normalizing blank end dates to null.
     *
     * @param edu The domain Education instance to convert.
     * @return An EducationTemplateModel with escaped string fields and a null endDate when the
     * source end date is blank.
     */
    private fun mapEducation(edu: Education): EducationTemplateModel =
        EducationTemplateModel(
            institution = LatexEscaper.escape(edu.institution.value),
            area = edu.area?.value?.let { LatexEscaper.escape(it) },
            studyType = edu.studyType?.value?.let { LatexEscaper.escape(it) },
            startDate = edu.startDate,
            endDate = edu.endDate?.takeIf { it.isNotBlank() }, // Normalize empty strings to null
            score = edu.score?.let(LatexEscaper::escape),
            url = edu.url?.value,
            courses = edu.courses?.map { LatexEscaper.escape(it) },
        )

    private fun mapSkill(skill: SkillCategory): SkillCategoryTemplateModel =
        SkillCategoryTemplateModel(
            name = LatexEscaper.escape(skill.name.value),
            level = skill.level?.let(LatexEscaper::escape),
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

    /**
     * Maps a domain Volunteer to a template-friendly VolunteerTemplateModel.
     *
     * @param vol The domain Volunteer to convert.
     * @return A VolunteerTemplateModel where text fields are escaped for LaTeX, `startDate` is an
     * empty string when absent, `endDate` is `null` when blank, and optional `summary` and `highlights`
     * are escaped when present.
     */
    private fun mapVolunteer(vol: Volunteer): VolunteerTemplateModel =
        VolunteerTemplateModel(
            organization = LatexEscaper.escape(vol.organization),
            position = LatexEscaper.escape(vol.position),
            url = vol.url?.value,
            startDate = vol.startDate?.let(LatexEscaper::escape) ?: "",
            endDate = vol.endDate?.takeIf { it.isNotBlank() }
                ?.let(LatexEscaper::escape), // Normalize empty strings to null
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
