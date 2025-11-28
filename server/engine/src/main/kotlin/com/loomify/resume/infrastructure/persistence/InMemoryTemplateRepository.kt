package com.loomify.resume.infrastructure.persistence

import com.loomify.resume.domain.Locale
import com.loomify.resume.domain.TemplateMetadata
import com.loomify.resume.domain.TemplateParams
import com.loomify.resume.domain.TemplateRepository
import org.springframework.stereotype.Repository

/**
 * In-memory stub implementation of TemplateRepository.
 * Returns a single engineering template metadata for initial development.
 */
@Repository
class InMemoryTemplateRepository : TemplateRepository {

    private val templates = listOf(
        TemplateMetadata(
            id = "engineering",
            name = "Engineering Resume",
            version = "0.1.0",
            description = "Engineering resume template (single-column focused for engineering profiles).",
            supportedLocales = listOf(Locale.EN, Locale.ES),
            previewUrl =
            "https://placehold.co/300x600.png",
            params = TemplateParams(
                colorPalette = "blue",
                fontFamily = "Roboto",
                spacing = "normal",
                density = "comfortable",
                customParams = mapOf(
                    "includePhoto" to true,
                    "highlightSkills" to true,
                ),
            ),
        ),
    )

    /**
     * Retrieves all available templates.
     * @return All template metadata
     */
    override suspend fun findAll(): List<TemplateMetadata> = templates

    /**
     * Finds a template by ID.
     * @param id The template ID
     * @return The template metadata if found
     */
    override suspend fun findById(id: String): TemplateMetadata? =
        templates.find { it.id == id }

    /**
     * Checks if a template exists.
     * @param id The template ID
     * @return true if the template exists
     */
    override suspend fun existsById(id: String): Boolean =
        templates.any { it.id == id }
}
