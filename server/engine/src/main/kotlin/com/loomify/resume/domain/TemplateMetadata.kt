package com.loomify.resume.domain

/**
 * Template metadata representing available PDF template configurations.
 * Used by the PDF Generation screen to display template options.
 * @param id Template identifier
 * @param name Template name
 * @param version Template version
 * @param description Optional template description
 * @param supportedLocales List of supported locales for this template
 * @param previewUrl Optional URL to a preview image of the template
 * @param params Optional template parameters for customization
 * @created 26/11/25
 */
data class TemplateMetadata(
    val id: String,
    val name: String,
    val version: String,
    val description: String? = null,
    val supportedLocales: List<Locale> = emptyList(),
    val previewUrl: String? = null,
    val params: TemplateParams? = null
)

/**
 * Template parameters for customizing PDF output.
 * Validated against the template's paramsSchema.
 */
data class TemplateParams(
    val colorPalette: String? = null,
    val fontFamily: String? = null,
    val spacing: String? = null,
    val density: String? = null,
    val customParams: Map<String, Any> = emptyMap(),
)
