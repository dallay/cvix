package com.loomify.resume.infrastructure.template.validator

import com.loomify.resume.domain.exception.LaTeXInjectionException
import com.loomify.resume.domain.model.ResumeData

/**
 * High-performance validator leveraging regex-based pattern matching
 * to detect LaTeX injection vectors. Implements fail-fast architecture
 * for optimal resource utilization.
 */
object TemplateValidator {
    @Suppress("MaxLineLength", "MaximumLineLength")
    private val DANGEROUS_PATTERN = Regex(
        """\\(?:input|include|write|openin|openout|immediate|def|let|expandafter|catcode|read|csname|newcommand|makeatletter)\b""",
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
     */
    @Suppress("CyclomaticComplexMethod") // Focused on data extraction
    private fun ResumeData.getAllStringContent() = sequence {
        // Basics
        yield(basics.name.value)
        yield(basics.email.value)
        basics.phone?.let { yield(it.value) }
        basics.summary?.let { yield(it.value) }

        // Work Experience
        work.forEach { workItem ->
            yield(workItem.company.value)
            yield(workItem.position.value)
            workItem.summary?.let { yield(it) }
            workItem.highlights?.forEach { yield(it.value) }
        }

        // Education
        education.forEach { edu ->
            yield(edu.institution.value)
            edu.area?.let { yield(it.value) }
            edu.studyType?.let { yield(it.value) }
            edu.courses?.forEach { yield(it) }
        }

        // Skills
        skills.forEach { category ->
            yield(category.name.value)
            category.keywords.forEach { yield(it.value) }
        }

        // Projects
        projects.forEach { project ->
            yield(project.name)
            yield(project.description)
            project.highlights?.forEach { yield(it) }
            project.keywords?.forEach { yield(it) }
        }

        // Languages
        languages.forEach { lang ->
            yield(lang.language)
        }
    }
}
