package com.cvix.resume.infrastructure.template.source

import com.cvix.resume.domain.TemplateMetadata
import com.cvix.resume.domain.TemplateMetadataLoader
import com.cvix.resume.domain.TemplateRepository
import com.cvix.resume.domain.TemplateSourceKeys
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

/**
 * Filesystem-based implementation of TemplateRepository.
 * Loads templates from the filesystem using TemplateMetadataLoader.
 * Scans for metadata.yaml files in subdirectories of the configured template path.
 * @created 11/12/25
 */
@Repository(TemplateSourceKeys.FILESYSTEM)
class FilesystemTemplateSourceRepository(
    private val templateSourceProperties: TemplateSourceProperties,
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
     * Loads templates from the filesystem by scanning for metadata.yaml files.
     * Discovers templates in subdirectories of the configured template path.
     * @return List of discovered template metadata
     */
    private fun loadTemplates(): List<TemplateMetadata> {
        val basePath = File(templateSourceProperties.source.path)

        if (!basePath.exists()) {
            log.warn("Template base path does not exist: {}", basePath.absolutePath)
            return emptyList()
        }

        if (!basePath.isDirectory) {
            log.warn("Template base path is not a directory: {}", basePath.absolutePath)
            return emptyList()
        }

        return try {
            val metadataFiles = findMetadataFiles(basePath)

            if (metadataFiles.isEmpty()) {
                log.warn("No metadata files found in: {}", basePath.absolutePath)
                return emptyList()
            }

            log.debug("Found {} metadata files in {}", metadataFiles.size, basePath.absolutePath)

            metadataFiles.mapNotNull { metadataFile ->
                try {
                    loadTemplateMetadata(metadataFile)
                } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                    log.warn(
                        "Failed to load template metadata from: {}",
                        metadataFile.absolutePath,
                        e,
                    )
                    null
                }
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            log.error("Error loading templates from filesystem: {}", basePath.absolutePath, e)
            emptyList()
        }
    }

    /**
     * Finds all metadata.yaml files recursively in the given directory.
     * @param baseDir The base directory to search
     * @return List of metadata files
     */
    private fun findMetadataFiles(baseDir: File): List<File> {
        return baseDir.walkTopDown()
            .filter { it.isFile && it.name == METADATA_FILENAME }
            .toList()
    }

    /**
     * Loads template metadata from a metadata.yaml file using the TemplateMetadataLoader.
     * Uses coroutine IO dispatcher for file operations.
     * @param metadataFile The metadata file to load
     * @return The loaded template metadata
     */
    private fun loadTemplateMetadata(metadataFile: File): TemplateMetadata {
        return kotlinx.coroutines.runBlocking {
            withContext(Dispatchers.IO) {
                FileInputStream(metadataFile).use { inputStream ->
                    templateMetadataLoader.loadTemplateMetadata(
                        inputStream,
                        metadataFile.absolutePath,
                    )
                }
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FilesystemTemplateSourceRepository::class.java)
        private const val METADATA_FILENAME = "metadata.yaml"
    }
}
