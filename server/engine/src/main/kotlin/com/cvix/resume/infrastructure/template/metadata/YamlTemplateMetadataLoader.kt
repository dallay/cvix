package com.cvix.resume.infrastructure.template.metadata

import com.cvix.resume.domain.Locale
import com.cvix.resume.domain.TemplateMetadata
import com.cvix.resume.domain.TemplateMetadataLoader
import com.cvix.resume.domain.TemplateParams
import com.cvix.subscription.domain.SubscriptionTier
import java.io.InputStream
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

/**
 * YAML adapter implementation of TemplateMetadataLoader.
 * Parses YAML files into TemplateMetadata objects using type-safe SnakeYAML Constructor.
 * This adapter is format-specific and can be easily replaced with JsonTemplateMetadataLoader
 * or XmlTemplateMetadataLoader without changing the discovery logic.
 * @created 11/12/25
 */
@Component
class YamlTemplateMetadataLoader : TemplateMetadataLoader {

    /**
     * Loads template metadata from a YAML input stream using type-safe parsing.
     * @param inputStream The input stream containing YAML metadata
     * @param sourceName The source name for logging (e.g., file path)
     * @return The parsed template metadata
     * @throws IllegalArgumentException if the stream content is invalid or required fields are missing
     * @see com.cvix.resume.domain.TemplateMetadata
     */
    override suspend fun loadTemplateMetadata(
        inputStream: InputStream,
        sourceName: String
    ): TemplateMetadata {
        return try {
            val metadata = loadRawMetadata(inputStream)
            validateAndTransform(metadata, sourceName)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            log.error("Error parsing metadata from {}: {}", sourceName, e.message)
            throw IllegalArgumentException(
                "Failed to load metadata from $sourceName: ${e.message}",
                e,
            )
        }
    }

    /**
     * Loads raw metadata using type-safe SnakeYAML Constructor.
     * @param inputStream The input stream containing YAML metadata
     * @return The raw metadata DTO
     */
    private fun loadRawMetadata(inputStream: InputStream): RawTemplateMetadata {
        val loaderOptions = LoaderOptions()
        val constructor = Constructor(RawTemplateMetadata::class.java, loaderOptions)
        val yaml = Yaml(constructor)

        return inputStream.bufferedReader().use { reader ->
            yaml.load(reader)
        }
    }

    /**
     * Validates and transforms raw metadata into domain TemplateMetadata.
     * @param raw The raw metadata DTO from YAML
     * @param sourceName The source name for error messages
     * @return The validated and transformed TemplateMetadata
     */
    private fun validateAndTransform(
        raw: RawTemplateMetadata,
        sourceName: String
    ): TemplateMetadata {
        requireNotNull(raw.id) { "Missing required field: id in $sourceName" }
        requireNotNull(raw.name) { "Missing required field: name in $sourceName" }
        requireNotNull(raw.version) { "Missing required field: version in $sourceName" }
        requireNotNull(raw.templatePath) { "Missing required field: templatePath in $sourceName" }

        val descriptions = raw.descriptions ?: emptyMap()
        val supportedLocales = raw.supportedLocales ?: emptyList()
        val params = raw.params ?: emptyMap<String, Any>()

        return TemplateMetadata(
            id = raw.id!!,
            name = raw.name!!,
            version = raw.version!!,
            descriptions = parseDescriptions(descriptions),
            supportedLocales = parseLocales(supportedLocales),
            templatePath = raw.templatePath!!,
            previewUrl = raw.previewUrl ?: "https://placehold.co/300x600.png",
            params = parseTemplateParams(params),
            requiredSubscriptionTier = parseSubscriptionTier(raw.requiredSubscriptionTier),
        ).also {
            log.debug(
                "Loaded template metadata from {}: id={}, name={}, version={}, requiredTier={}",
                sourceName,
                it.id,
                it.name,
                it.version,
                it.requiredSubscriptionTier,
            )
        }
    }

    /**
     * Parses localized descriptions from metadata.
     * Expected format in YAML:
     * ```yaml
     * descriptions:
     *   en: English description
     *   es: Spanish description
     * ```
     * @param descriptionsData The raw descriptions data from YAML
     * @return Map of Locale to description
     */
    private fun parseDescriptions(descriptionsData: Map<String, String>): Map<Locale, String> =
        descriptionsData.mapNotNull { (localeCode, description) ->
            try {
                val locale = Locale.from(localeCode)
                locale to description
            } catch (_: IllegalArgumentException) {
                log.warn("Unknown locale code in descriptions: {}", localeCode)
                null
            }
        }.toMap()

    /**
     * Parses supported locales from metadata.
     * Expected format in YAML:
     * ```yaml
     * supportedLocales:
     *   - en
     *   - es
     * ```
     * @param localesData The raw locales data from YAML
     * @return List of supported Locales
     */
    private fun parseLocales(localesData: List<String>): List<Locale> =
        localesData.mapNotNull { localeCode ->
            try {
                Locale.from(localeCode)
            } catch (_: IllegalArgumentException) {
                log.warn("Unknown locale code in supportedLocales: {}", localeCode)
                null
            }
        }

    /**
     * Parses subscription tier from metadata.
     * Expected format in YAML:
     * ```yaml
     * requiredSubscriptionTier: BASIC
     * ```
     * @param tierValue The raw tier value from YAML
     * @return The parsed SubscriptionTier, defaults to FREE if not specified or invalid
     */
    private fun parseSubscriptionTier(tierValue: String?): SubscriptionTier {
        if (tierValue.isNullOrBlank()) {
            return SubscriptionTier.FREE
        }

        return try {
            SubscriptionTier.valueOf(tierValue.uppercase().trim())
        } catch (_: IllegalArgumentException) {
            log.warn("Unknown subscription tier '{}', defaulting to FREE", tierValue)
            SubscriptionTier.FREE
        }
    }

    /**
     * Parses template parameters from metadata.
     * Expected format in YAML:
     * ```yaml
     * params:
     *   colorPalette:
     *     default: blue
     *     options: [blue, green, red]
     *   fontFamily:
     *     default: Roboto
     *     options: [Roboto, OpenSans]
     *   spacing:
     *     default: normal
     *   density:
     *     default: comfortable
     * ```
     * @param paramsData The raw params data from YAML
     * @return Template parameters or null if not specified
     */
    private fun parseTemplateParams(paramsData: Map<String, Any>): TemplateParams = TemplateParams(
        colorPalette = extractDefaultValue(paramsData["colorPalette"]),
        fontFamily = extractDefaultValue(paramsData["fontFamily"]),
        spacing = extractDefaultValue(paramsData["spacing"]),
        density = extractDefaultValue(paramsData["density"]),
        customParams = extractCustomParams(paramsData),
    )

    /**
     * Extracts the default value from a parameter configuration.
     * Handles both simple string values and nested objects with "default" key.
     * @param paramValue The parameter value from YAML
     * @return The default value as a string or null
     */
    private fun extractDefaultValue(paramValue: Any?): String? {
        return when (paramValue) {
            is String -> paramValue
            is Map<*, *> -> paramValue["default"] as? String
            else -> null
        }
    }

    /**
     * Extracts custom parameters that are not standard template parameters.
     * @param paramsMap The full params map from YAML
     * @return Map of custom parameters
     */
    private fun extractCustomParams(paramsMap: Map<String, Any>): Map<String, Any> {
        return paramsMap
            .filterKeys { key ->
                key !in listOf("colorPalette", "fontFamily", "spacing", "density")
            }
    }

    companion object {
        private val log = LoggerFactory.getLogger(YamlTemplateMetadataLoader::class.java)
    }
}
