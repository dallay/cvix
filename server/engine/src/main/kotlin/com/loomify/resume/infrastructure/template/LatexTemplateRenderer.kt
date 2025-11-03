package com.loomify.resume.infrastructure.template

import com.loomify.resume.domain.exception.LaTeXInjectionException
import com.loomify.resume.domain.exception.TemplateRenderingException
import com.loomify.resume.domain.model.Education
import com.loomify.resume.domain.model.Language
import com.loomify.resume.domain.model.PersonalInfo
import com.loomify.resume.domain.model.Project
import com.loomify.resume.domain.model.ResumeData
import com.loomify.resume.domain.model.SkillCategory
import com.loomify.resume.domain.model.WorkExperience
import com.loomify.resume.domain.port.TemplateRendererPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.stringtemplate.v4.STGroupDir

/**
 * Adapter that renders LaTeX templates using StringTemplate 4.
 * Implements security measures to prevent LaTeX injection attacks.
 */
@Component
class LatexTemplateRenderer(
    private val templateGroup: STGroupDir
) : TemplateRendererPort {

    private val logger = LoggerFactory.getLogger(javaClass)

    // ...existing code...

    @Suppress("TooGenericExceptionCaught") // wrap StringTemplate runtime exceptions
    override fun render(resumeData: ResumeData, locale: String): String {
        try {
            // Security check: Scan for malicious LaTeX commands
            validateContent(resumeData)

            // Load template based on locale
            val templateName = "resume-template-$locale"
            val template = templateGroup.getInstanceOf(templateName)
                ?: throw TemplateRenderingException("Template not found for locale: $locale")

            // Populate template with escaped data
            populateTemplate(template, resumeData, locale)

            // Render template
            val rendered = template.render()

            logger.debug("Successfully rendered resume template for locale: $locale")

            return rendered
        } catch (e: LaTeXInjectionException) {
            throw e // Re-throw security exceptions
        } catch (e: Exception) {
            logger.error("Failed to render template", e)
            throw TemplateRenderingException("Failed to render resume template: ${e.message}", e)
        }
    }

    private fun validateContent(resumeData: ResumeData) {
        // Check all string content for dangerous LaTeX commands
        val allContent = buildList {
            add(resumeData.basics.fullName.value)
            add(resumeData.basics.email.value)
            resumeData.basics.phone?.let { add(it.value) }
            resumeData.basics.summary?.let { add(it.value) }
            resumeData.work.forEach {
                add(it.company.value)
                add(it.position.value)
                it.summary?.let { desc -> add(desc) }
            }
            resumeData.education.forEach {
                add(it.institution.value)
                add(it.area.value)
                add(it.studyType.value)
            }
            resumeData.skills.forEach { category ->
                add(category.name.value)
                category.keywords.forEach { skill -> add(skill.value) }
            }
        }

        allContent.forEach { content ->
            DANGEROUS_COMMANDS.forEach { command ->
                if (content.contains(command, ignoreCase = true)) {
                    throw LaTeXInjectionException(
                        "Potentially malicious LaTeX command detected: $command",
                    )
                }
            }
        }
    }

    private fun escapeLatex(text: String): String {
        var escaped = text
        LATEX_ESCAPE_CHARS.forEach { (char, replacement) ->
            escaped = escaped.replace(char, replacement)
        }
        return escaped
    }

    private fun populateTemplate(template: org.stringtemplate.v4.ST, resumeData: ResumeData, locale: String) {
        // Add content presence flags for adaptive layout
        template.add("hasProjects", resumeData.projects.isNotEmpty())
        template.add("hasLanguages", resumeData.languages.isNotEmpty())

        addPersonalInfo(template, resumeData.basics)
        addWorkExperience(template, resumeData.work, locale)
        addEducation(template, resumeData.education, locale)
        addSkills(template, resumeData.skills)
        addLanguages(template, resumeData.languages)
        addProjects(template, resumeData.projects)
    }

    private fun addPersonalInfo(template: org.stringtemplate.v4.ST, basics: PersonalInfo) {
        val nameParts = basics.fullName.value.split(" ", limit = 2)
        template.add("firstName", escapeLatex(nameParts[0]))
        template.add("lastName", escapeLatex(nameParts.getOrNull(1) ?: ""))
        template.add("email", escapeLatex(basics.email.value))
        basics.phone?.let { template.add("phone", escapeLatex(it.value)) }
        basics.location?.city?.let { template.add("location", escapeLatex(it)) }
        basics.profiles.find { it.network == "LinkedIn" }?.let {
            template.add("linkedin", escapeLatex(it.url))
        }
        basics.profiles.find { it.network == "GitHub" }?.let {
            template.add("github", escapeLatex(it.url))
        }
        basics.url?.let { template.add("website", escapeLatex(it.value)) }
        basics.summary?.let { template.add("summary", escapeLatex(it.value)) }
    }

    private fun addWorkExperience(template: org.stringtemplate.v4.ST, workList: List<WorkExperience>, locale: String) {
        if (workList.isNotEmpty()) {
            val mapped = workList.map { work ->
                mapOf(
                    "company" to escapeLatex(work.company.value),
                    "position" to escapeLatex(work.position.value),
                    "period" to escapeLatex(work.formatPeriod(locale)),
                    "location" to (work.location?.let { escapeLatex(it) } ?: ""),
                    "description" to (work.summary?.let { escapeLatex(it) } ?: ""),
                )
            }
            template.add("work", mapped)
        }
    }

    private fun addEducation(template: org.stringtemplate.v4.ST, eduList: List<Education>, locale: String) {
        if (eduList.isNotEmpty()) {
            val mapped = eduList.map { edu ->
                mapOf(
                    "institution" to escapeLatex(edu.institution.value),
                    "degree" to escapeLatex(edu.studyType.value),
                    "period" to escapeLatex(edu.formatPeriod(locale)),
                    "area" to escapeLatex(edu.area.value),
                    "score" to (edu.score?.let { escapeLatex(it) } ?: ""),
                )
            }
            template.add("education", mapped)
        }
    }

    private fun addSkills(template: org.stringtemplate.v4.ST, skillsList: List<SkillCategory>) {
        if (skillsList.isNotEmpty()) {
            val mapped = skillsList.map { category ->
                mapOf(
                    "name" to escapeLatex(category.name.value),
                    "keywords" to category.keywords.map { escapeLatex(it.value) },
                )
            }
            template.add("skills", mapped)
        }
    }

    private fun addLanguages(template: org.stringtemplate.v4.ST, langList: List<Language>) {
        if (langList.isNotEmpty()) {
            val mapped = langList.map { lang ->
                mapOf(
                    "language" to escapeLatex(lang.language),
                    "fluency" to (lang.fluency?.let { escapeLatex(it) } ?: ""),
                )
            }
            template.add("languages", mapped)
        }
    }

    private fun addProjects(template: org.stringtemplate.v4.ST, projectList: List<Project>) {
        if (projectList.isNotEmpty()) {
            val mapped = projectList.map { project ->
                mapOf(
                    "name" to escapeLatex(project.name),
                    "description" to (project.description?.let { escapeLatex(it) } ?: ""),
                    "url" to (project.url?.let { escapeLatex(it) } ?: ""),
                )
            }
            template.add("projects", mapped)
        }
    }

    companion object {
        // LaTeX special characters that need escaping
        private val LATEX_ESCAPE_CHARS = mapOf(
            "\\" to "\\textbackslash{}",
            "{" to "\\{",
            "}" to "\\}",
            "$" to "\\$",
            "&" to "\\&",
            "%" to "\\%",
            "#" to "\\#",
            "_" to "\\_",
            "^" to "\\textasciicircum{}",
            "~" to "\\textasciitilde{}",
        )

        // Dangerous LaTeX commands that should never appear in user content
        private val DANGEROUS_COMMANDS = listOf(
            "\\input",
            "\\include",
            "\\write",
            "\\openin",
            "\\openout",
            "\\immediate",
            "\\def",
            "\\let",
            "\\expandafter",
        )
    }
}
