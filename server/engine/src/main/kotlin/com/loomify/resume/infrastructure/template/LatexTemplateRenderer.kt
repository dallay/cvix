package com.loomify.resume.infrastructure.template

import com.loomify.resume.domain.exception.LaTeXInjectionException
import com.loomify.resume.domain.exception.TemplateRenderingException
import com.loomify.resume.domain.model.ResumeData
import com.loomify.resume.domain.port.TemplateRenderer
import com.loomify.resume.infrastructure.template.mapper.ResumeTemplateMapper
import com.loomify.resume.infrastructure.template.renders.UrlRenderer
import com.loomify.resume.infrastructure.template.validator.TemplateValidator
import java.util.Locale
import java.util.ResourceBundle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.stringtemplate.v4.STGroupFile

/**
 * Adapter that renders LaTeX templates using StringTemplate 4.
 * Implements security measures to prevent LaTeX injection attacks.
 *
 * Uses STGroupFile to load the modular engineering.stg template group.
 */
@Component
@Suppress("TooManyFunctions") // Template builders need multiple focused functions
class LatexTemplateRenderer : TemplateRenderer {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Suppress("TooGenericExceptionCaught")
    override fun render(resumeData: ResumeData, locale: String): String {
        try {
            // Security check: Scan for malicious LaTeX commands
            TemplateValidator.validateContent(resumeData)

            // Convert domain model to template-friendly model
            val templateModel = ResumeTemplateMapper.toTemplateModel(resumeData)

            // Load i18n translations for the specified locale
            val i18nMap = loadI18nTranslations(locale)

            // Load template group from classpath
            val templatePath = "templates/resume/engineering/engineering.stg"
            val group = STGroupFile(templatePath)

            // Register custom URL renderer for String attributes
            group.registerRenderer(String::class.java, UrlRenderer())

            // Get the main template
            val template = group.getInstanceOf("baseResume")
                ?: throw TemplateRenderingException("Template 'baseResume' not found in $templatePath")

            template.add("resumeData", templateModel)
            template.add("i18n", i18nMap)
            template.add("locale", locale)

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

    private fun loadI18nTranslations(locale: String): Map<String, String> {
        val resourceLocale = Locale.forLanguageTag(locale)
        val bundle = ResourceBundle.getBundle("messages_template", resourceLocale)

        return bundle.keys.asSequence().associateWith { key ->
            bundle.getString(key)
        }
    }
}
