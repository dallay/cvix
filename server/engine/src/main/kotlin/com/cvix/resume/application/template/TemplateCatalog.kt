package com.cvix.resume.application.template

import com.cvix.common.domain.Service
import com.cvix.resume.domain.TemplateMetadata
import com.cvix.resume.domain.TemplateRepository
import org.slf4j.LoggerFactory

/**
 * Catalog service for managing and retrieving template metadata.
 * @created 26/11/25
 */
@Service
class TemplateCatalog(private val templateRepository: TemplateRepository) {
    /**
     * Lists available templates with an optional limit.
     * @param limit Maximum number of templates to return
     * @return List of template metadata
     */
    suspend fun listTemplates(limit: Int?): List<TemplateMetadata> {
        log.debug("Fetching templates with limit={}", limit)
        val templates = templateRepository.findAll()
        return limit
            ?.takeIf { it > 0 }
            ?.let { templates.take(it) }
            ?: templates
    }

    companion object {
        private val log = LoggerFactory.getLogger(TemplateCatalog::class.java)
    }
}
