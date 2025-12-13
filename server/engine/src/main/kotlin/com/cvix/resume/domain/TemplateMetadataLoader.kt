package com.cvix.resume.domain

import java.io.InputStream

/**
 * Adapter interface for loading template metadata from different formats.
 * Implementations can support YAML, JSON, XML, or any other format.
 * This allows easy format switching without changing the discovery logic.
 *
 * This interface is framework-agnostic and only depends on Java/Kotlin standard library.
 * @created 11/12/25
 */
fun interface TemplateMetadataLoader {
    /**
     * Loads template metadata from an input stream.
     * @param inputStream The input stream containing the metadata file content
     * @param sourceName Source name for logging/error messages (e.g., file path)
     * @return The loaded template metadata
     * @throws IllegalArgumentException if the stream content is invalid or required fields are missing
     * @see TemplateMetadata
     */
    suspend fun loadTemplateMetadata(inputStream: InputStream, sourceName: String): TemplateMetadata
}
