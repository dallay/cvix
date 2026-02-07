package com.cvix.resume.domain

import java.io.InputStream

/**
 * Adapter interface for loading template metadata from different formats.
 * Implementations can support YAML, JSON, XML, or any other format.
 * This allows easy format switching without changing the discovery logic.
 *
 * InputStream ownership and blocking I/O:
 * - The caller owns the provided [InputStream] and is responsible for closing it. Implementations
 *   MUST NOT close the stream. Read from it and return; the lifecycle is managed by the caller.
 * - This function is suspendable and may be invoked from coroutine contexts sensitive to blocking.
 *   Implementations MUST perform any blocking parsing or I/O inside `withContext(Dispatchers.IO)`
 *   (e.g., reading bytes from the stream, parsing large files) before returning [TemplateMetadata].
 *   Non-blocking, CPU-light transformations can remain in the current context.
 *
 * This interface is framework-agnostic and only depends on Java/Kotlin standard library.
 * @created 11/12/25
 */
fun interface TemplateMetadataLoader {
    /**
     * Loads template metadata from an input stream.
     * @param inputStream The input stream containing the metadata file content. The caller is responsible for
     *                    closing this stream; implementations must not close it.
     * @param sourceName Source name for logging/error messages (e.g., file path)
     * @return The loaded template metadata
     * @throws IllegalArgumentException if the stream content is invalid or required fields are missing
     * @see TemplateMetadata
     */
    suspend fun loadTemplateMetadata(inputStream: InputStream, sourceName: String): TemplateMetadata
}
