package com.cvix.resume.domain

/**
 * Repository port for Template metadata access.
 * Defines the contract for retrieving available PDF templates.
 */
interface TemplateRepository {
    /**
     * Retrieves all available templates.
     * @return All template metadata
     */
    suspend fun findAll(): List<TemplateMetadata>

    /**
     * Finds a template by ID.
     * @param id The template ID
     * @return The template metadata if found
     */
    suspend fun findById(id: String): TemplateMetadata?

    /**
     * Checks if a template exists.
     * @param id The template ID
     * @return true if the template exists
     */
    suspend fun existsById(id: String): Boolean
}
