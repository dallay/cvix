package com.cvix.resume.infrastructure.template.metadata

/**
 * Data Transfer Object for raw YAML metadata.
 * Used by SnakeYAML Constructor for type-safe parsing.
 * All fields are nullable to allow validation after parsing.
 */
data class RawTemplateMetadata(
    var id: String? = null,
    var name: String? = null,
    var version: String? = null,
    var descriptions: Map<String, String>? = null,
    var supportedLocales: List<String>? = null,
    var requiredSubscriptionTier: String? = null,
    var templatePath: String? = null,
    var previewUrl: String? = null,
    var params: Map<String, Any>? = null,
)
