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
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.stringtemplate.v4.STGroupFile

/**
 * Adapter that renders LaTeX templates using StringTemplate 4.
 * Implements security measures to prevent LaTeX injection attacks.
 *
 * Uses STGroupFile to load the modular engineering.stg template group.
 * Template groups and i18n bundles are cached for performance.
 */
@Component
@Suppress("TooManyFunctions") // Template builders need multiple focused functions
class LatexTemplateRenderer : TemplateRenderer {

    private val logger = LoggerFactory.getLogger(javaClass)

    // Thread-safe cache for STGroupFile instances keyed by template path
    private val templateGroupCache = ConcurrentHashMap<String, STGroupFile>()

    // Thread-safe cache for i18n translations keyed by locale
    private val i18nCache = ConcurrentHashMap<String, Map<String, String>>()

    @Suppress("TooGenericExceptionCaught")
    override fun render(resumeData: ResumeData, locale: String): String {
        try {
            // Security check: Scan for malicious LaTeX commands
            TemplateValidator.validateContent(resumeData)

            // Convert domain model to template-friendly model
            val templateModel = ResumeTemplateMapper.toTemplateModel(resumeData)

            // Load i18n translations for the specified locale (cached)
            val i18nMap = loadI18nTranslations(locale)

            // Load template group from classpath (cached)
            val templatePath = "templates/resume/engineering/engineering.stg"
            val group = getOrCreateTemplateGroup(templatePath)

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

    /**
     * Retrieves or creates a cached STGroupFile for the given template path.
     * Thread-safe using ConcurrentHashMap.computeIfAbsent.
     */
    private fun getOrCreateTemplateGroup(templatePath: String): STGroupFile =
        templateGroupCache.computeIfAbsent(templatePath) {
            STGroupFile(templatePath).apply {
                // Register custom URL renderer for String attributes
                registerRenderer(String::class.java, UrlRenderer())
                logger.debug("Created and cached STGroupFile for template: $templatePath")
            }
        }

    /**
     * Loads and caches i18n translations for the specified locale.
     * Thread-safe using ConcurrentHashMap.computeIfAbsent.
     */
    private fun loadI18nTranslations(locale: String): Map<String, String> =
        i18nCache.computeIfAbsent(locale) {
            val resourceLocale = Locale.forLanguageTag(locale)
            val bundle = ResourceBundle.getBundle("messages_template", resourceLocale)

            bundle.keys.asSequence().associateWith { key ->
                bundle.getString(key)
            }.also {
                logger.debug("Loaded and cached i18n translations for locale: $locale")
            }
        }
}
