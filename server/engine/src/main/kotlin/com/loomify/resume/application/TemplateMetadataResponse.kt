package com.loomify.resume.application

import com.loomify.common.domain.bus.query.Response
import com.loomify.resume.domain.TemplateMetadata

/**
 * Response DTO for Template Metadata.
 *
 * @param id Template identifier
 * @param name Template name
 * @param version Template version
 * @param description Optional template description
 * @param paramsSchema JSON Schema for template parameters
 * @created 26/11/25
 */
data class TemplateMetadataResponse(
    val id: String,
    val name: String,
    val version: String,
    val description: String? = null,
    val paramsSchema: String? = null, // JSON Schema for template parameters
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
                paramsSchema = domain.paramsSchema,
                description = domain.description,
            )
        }
    }
}

/**
 * Represents a collection of resume document responses.
 *
 * @property data A list of `ResumeDocumentResponse` objects.
 */
data class TemplateMetadataResponses(val data: List<TemplateMetadataResponse>) : Response {
    companion object {
        /**
         * Converts a list of TemplateMetadata objects to a TemplateMetadataResponses.
         *
         * @param domains The list of domain TemplateMetadata objects
         * @return The corresponding TemplateMetadataResponses
         */
        fun from(domains: List<TemplateMetadata>): TemplateMetadataResponses {
            val responses = domains.map { TemplateMetadataResponse.from(it) }
            return TemplateMetadataResponses(responses)
        }
    }
}
