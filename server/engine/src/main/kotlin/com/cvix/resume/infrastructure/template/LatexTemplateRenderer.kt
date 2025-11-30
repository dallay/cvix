package com.cvix.resume.infrastructure.template

import com.cvix.resume.domain.Resume
import com.cvix.resume.domain.TemplateRenderer
import com.cvix.resume.domain.exception.LaTeXInjectionException
import com.cvix.resume.domain.exception.TemplateRenderingException
import com.cvix.resume.infrastructure.template.mapper.ResumeTemplateMapper
import com.cvix.resume.infrastructure.template.renders.UrlRenderer
import com.cvix.resume.infrastructure.template.validator.TemplateValidator
import java.time.Clock
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
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
class LatexTemplateRenderer(
    private val clock: Clock = Clock.systemDefaultZone(),
    private val templatePath: String = "templates/resume/engineering/engineering.stg" // Default path
) : TemplateRenderer {

    private val logger = LoggerFactory.getLogger(javaClass)

    // Thread-safe cache for STGroupFile instances keyed by template path
    private val templateGroupCache = ConcurrentHashMap<String, STGroupFile>()

    // Thread-safe cache for i18n translations keyed by locale
    private val i18nCache = ConcurrentHashMap<String, Map<String, String>>()

    @Suppress("TooGenericExceptionCaught")
    override fun render(resume: Resume, locale: String): String {
        try {
            // Security check: Scan for malicious LaTeX commands
            TemplateValidator.validateContent(resume)

            // Convert domain model to template-friendly model
            val templateModel = ResumeTemplateMapper.toTemplateModel(resume)

            // Load i18n translations for the specified locale (cached)
            val i18nMap = loadI18nTranslations(locale)

            // Load template group from classpath (cached)
            val group = getOrCreateTemplateGroup(templatePath)

            // Get the main template
            val template = group.getInstanceOf("baseResume")
                ?: throw TemplateRenderingException("Template 'baseResume' not found in $templatePath")

            template.add("resumeData", templateModel)
            template.add("i18n", i18nMap)
            template.add("locale", locale)
            template.add("lastUpdated", formatLastUpdatedDate(locale))

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
            val bundle = try {
                ResourceBundle.getBundle("messages_template", resourceLocale)
            } catch (_: MissingResourceException) {
                logger.warn("i18n bundle not found for locale $locale, falling back to English")
                ResourceBundle.getBundle("messages_template", Locale.ENGLISH)
            }

            bundle.keys.asSequence().associateWith { key ->
                bundle.getString(key)
            }.also {
                logger.debug("Loaded and cached i18n translations for locale: $locale")
            }
        }

    /**
     * Formats the current month and year according to the specified locale.
     * Returns a localized string like "November 2025" (en) or "noviembre de 2025" (es).
     */
    private fun formatLastUpdatedDate(locale: String): String {
        val resourceLocale = Locale.forLanguageTag(locale)
        val pattern = if (resourceLocale.language == "es") "MMMM 'de' yyyy" else "MMMM yyyy"
        val formatter = DateTimeFormatter.ofPattern(pattern, resourceLocale)
        return YearMonth.now(clock).format(formatter)
    }
}
