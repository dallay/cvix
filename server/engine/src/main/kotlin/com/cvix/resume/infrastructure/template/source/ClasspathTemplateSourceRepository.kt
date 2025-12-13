package com.cvix.resume.infrastructure.template.source

import com.cvix.resume.domain.TemplateMetadata
import com.cvix.resume.domain.TemplateMetadataLoader
import com.cvix.resume.domain.TemplateRepository
import com.cvix.resume.domain.TemplateSourceKeys
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.stereotype.Repository

/**
 * Template source that loads templates from the classpath.
 * base template path for resume templates: templates/resume/
 * @created 11/12/25
 */
@Repository(TemplateSourceKeys.CLASSPATH)
@Primary
class ClasspathTemplateSourceRepository(
    private val resourcePatternResolver: ResourcePatternResolver,
    private val templateMetadataLoader: TemplateMetadataLoader
) : TemplateRepository {

    private val templates: List<TemplateMetadata> by lazy { loadTemplates() }

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

    /**
     * Loads templates from the classpath by scanning for metadata files.
     * Discovers templates by finding metadata.yaml files under templates/resume/
     * Uses the TemplateMetadataLoader adapter to parse each metadata file.
     * @return List of discovered template metadata
     */
    private fun loadTemplates(): List<TemplateMetadata> {
        return try {
            val pattern = "$TEMPLATES_BASE_PATH/**/metadata.yaml"
            val resources = resourcePatternResolver.getResources(pattern)

            if (resources.isEmpty()) {
                log.warn("No metadata files found matching pattern: $pattern")
                return emptyList()
            }

            log.debug("Found {} metadata files", resources.size)

            resources.mapNotNull { resource ->
                try {
                    val sourceName = resource.filename ?: resource.description
                    runBlocking {
                        resource.inputStream.use { inputStream ->
                            templateMetadataLoader.loadTemplateMetadata(inputStream, sourceName)
                        }
                    }
                } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                    log.warn("Failed to load template metadata from: ${resource.filename}", e)
                    null
                }
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            log.error("Error loading templates from classpath", e)
            emptyList()
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ClasspathTemplateSourceRepository::class.java)
        private const val TEMPLATES_BASE_PATH = "classpath:templates/resume"
    }
}
