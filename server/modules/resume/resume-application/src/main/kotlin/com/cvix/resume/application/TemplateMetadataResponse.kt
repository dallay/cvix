package com.cvix.resume.application

import com.cvix.common.domain.bus.query.Response
import com.cvix.resume.domain.Locale
import com.cvix.resume.domain.TemplateMetadata
import com.cvix.resume.domain.TemplateParams

/**
 * Response DTO for Template Metadata.
 *
 * @param id Template identifier
 * @param name Template name
 * @param version Template version
 * @param descriptions Localized descriptions of the template
 * @param supportedLocales List of supported locales for this template
 * @param previewUrl Optional URL to a preview image of the template
 * @param params Optional template parameters for customization
 * @created 26/11/25
 */
data class TemplateMetadataResponse(
    val id: String,
    val name: String,
    val version: String,
    val descriptions: Map<Locale, String> = emptyMap(),
    val supportedLocales: List<Locale> = emptyList(),
    val previewUrl: String? = null,
    val params: TemplateParams? = null
) : Response {
    companion object {
        /**
         * Converts a domain TemplateMetadata object to a TemplateMetadataResponse.
         *
         * @param domain The domain TemplateMetadata object
         * @return The corresponding TemplateMetadataResponse
         */
        fun from(domain: TemplateMetadata): TemplateMetadataResponse {
            return TemplateMetadataResponse(
                id = domain.id,
                name = domain.name,
                version = domain.version,
                descriptions = domain.descriptions,
                supportedLocales = domain.supportedLocales,
                previewUrl = domain.previewUrl,
                params = domain.params,
            )
        }
    }
}

/**
 * Represents a collection of template metadata responses.
 *
 * @property data A list of `TemplateMetadataResponse` objects.
 */
data class TemplateMetadataResponses(val data: List<TemplateMetadataResponse>) : Response {
    companion object {
        /**
         * Converts a list of TemplateMetadata objects to a TemplateMetadataResponses.
         *
         * @param domains The list of domain TemplateMetadata objects
         * @return The corresponding TemplateMetadataResponses
         */
        fun from(domains: List<TemplateMetadata>): TemplateMetadataResponses =
            TemplateMetadataResponses(domains.map(TemplateMetadataResponse::from))
    }
}
