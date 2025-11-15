package com.loomify.resume.infrastructure.template.validator

import com.loomify.resume.domain.exception.LaTeXInjectionException
import com.loomify.resume.domain.model.ResumeData

/**
 * High-performance validator leveraging regex-based pattern matching
 * to detect LaTeX injection vectors. Implements fail-fast architecture
 * for optimal resource utilization.
 *
 * Security Model:
 * - Primary defense: Proactive validation scanning all user fields
 * - Secondary defense: LatexEscaper.escape() in ResumeTemplateMapper
 * - Both layers together form the complete security boundary
 */
object TemplateValidator {
    @Suppress("MaxLineLength", "MaximumLineLength")
    private val DANGEROUS_PATTERN = Regex(
        """\\(?:input|include|write|openin|openout|immediate|def|let|expandafter|catcode|read|csname|newcommand|renewcommand|makeatletter|makeatother|loop|repeat|newwrite|newread|jobname|special|message|errmessage|typeout)\b""",
        RegexOption.IGNORE_CASE,
    )

    /**
     * Validates resume data using single-pass scanning with early termination.
     *
     * @throws LaTeXInjectionException on first detection of malicious command
     */
    fun validateContent(resumeData: ResumeData) {
        val violation = resumeData.getAllStringContent()
            .mapNotNull { content -> DANGEROUS_PATTERN.find(content)?.value }
            .firstOrNull()

        violation?.let {
            throw LaTeXInjectionException("Malicious LaTeX command detected: $it")
        }
    }

    /**
     * Extracts all user-provided strings using lazy sequence evaluation
     * for memory-efficient processing at scale.
     *
     * Covers all fields in ResumeData to provide comprehensive validation
     * as the first line of defense against LaTeX injection attacks.
     */
    @Suppress("CyclomaticComplexMethod") // Focused on data extraction
    private fun ResumeData.getAllStringContent() = sequence {
        // Basics - Personal Information
        yield(basics.name.value)
        yield(basics.email.value)
        basics.label?.let { yield(it.value) }
        basics.phone?.let { yield(it.value) }
        basics.url?.let { yield(it.value) }
        basics.summary?.let { yield(it.value) }

        // Basics - Location
        basics.location?.let { location ->
            location.address?.let { yield(it) }
            location.postalCode?.let { yield(it) }
            location.city?.let { yield(it) }
            location.countryCode?.let { yield(it) }
            location.region?.let { yield(it) }
        }

        // Basics - Social Profiles
        basics.profiles.forEach { profile ->
            yield(profile.network)
            yield(profile.username)
            yield(profile.url)
        }

        // Work Experience
        work.forEach { workItem ->
            yield(workItem.company.value)
            yield(workItem.position.value)
            yield(workItem.startDate)
            workItem.endDate?.let { yield(it) }
            workItem.location?.let { yield(it) }
            workItem.summary?.let { yield(it) }
            workItem.url?.let { yield(it.value) }
            workItem.highlights?.forEach { yield(it.value) }
        }

        // Education
        education.forEach { edu ->
            yield(edu.institution.value)
            yield(edu.startDate)
            edu.endDate?.let { yield(it) }
            edu.area?.let { yield(it.value) }
            edu.studyType?.let { yield(it.value) }
            edu.score?.let { yield(it) }
            edu.url?.let { yield(it.value) }
            edu.courses?.forEach { yield(it) }
        }

        // Skills
        skills.forEach { category ->
            yield(category.name.value)
            category.level?.let { yield(it) }
            category.keywords.forEach { yield(it.value) }
        }

        // Projects
        projects.forEach { project ->
            yield(project.name)
            yield(project.description)
            project.url?.let { yield(it) }
            project.startDate?.let { yield(it.toString()) }
            project.endDate?.let { yield(it.toString()) }
            project.entity?.let { yield(it) }
            project.type?.let { yield(it) }
            project.highlights?.forEach { yield(it) }
            project.keywords?.forEach { yield(it) }
            project.roles?.forEach { yield(it) }
        }

        // Volunteer
        volunteer.forEach { vol ->
            yield(vol.organization)
            yield(vol.position)
            vol.url?.let { yield(it.value) }
            vol.startDate?.let { yield(it) }
            vol.endDate?.let { yield(it) }
            vol.summary?.let { yield(it) }
            vol.highlights?.forEach { yield(it) }
        }

        // Awards
        awards.forEach { award ->
            yield(award.title)
            award.date?.let { yield(it) }
            award.awarder?.let { yield(it) }
            award.summary?.let { yield(it) }
        }

        // Certificates
        certificates.forEach { cert ->
            yield(cert.name)
            cert.date?.let { yield(it) }
            cert.issuer?.let { yield(it) }
            cert.url?.let { yield(it.value) }
        }

        // Publications
        publications.forEach { pub ->
            yield(pub.name)
            pub.publisher?.let { yield(it) }
            pub.releaseDate?.let { yield(it) }
            pub.url?.let { yield(it.value) }
            pub.summary?.let { yield(it) }
        }

        // Interests
        interests.forEach { interest ->
            yield(interest.name)
            interest.keywords?.forEach { yield(it) }
        }

        // Languages
        languages.forEach { lang ->
            yield(lang.language)
            yield(lang.fluency)
        }

        // References
        references.forEach { ref ->
            yield(ref.name)
            ref.reference?.let { yield(it) }
        }
    }
}
