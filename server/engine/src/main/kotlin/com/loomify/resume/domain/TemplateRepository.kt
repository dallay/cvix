package com.loomify.resume.domain

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Repository port for Template metadata access.
 * Defines the contract for retrieving available PDF templates.
 */
interface TemplateRepository {
    /**
     * Retrieves all available templates.
     * @return All template metadata
     */
    fun findAll(): Flux<TemplateMetadata>

    /**
     * Finds a template by ID.
     * @param id The template ID
     * @return The template metadata if found
     */
    fun findById(id: String): Mono<TemplateMetadata>

    /**
     * Checks if a template exists.
     * @param id The template ID
     * @return true if the template exists
     */
    fun existsById(id: String): Mono<Boolean>
}
