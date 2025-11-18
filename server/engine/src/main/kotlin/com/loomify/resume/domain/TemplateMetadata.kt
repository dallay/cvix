package com.loomify.resume.domain

/**
 * Template metadata representing available PDF template configurations.
 * Used by the PDF Generation screen to display template options.
 */
data class TemplateMetadata(
    val id: String,
    val name: String,
    val version: String,
    val paramsSchema: String? = null, // JSON Schema for template parameters
    val description: String? = null,
    val previewUrl: String? = null,
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
