package com.loomify.resume.infrastructure.persistence

import com.loomify.resume.domain.TemplateMetadata
import com.loomify.resume.domain.TemplateRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

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
            paramsSchema = """
                {
                  "type": "object",
                  "properties": {
                    "colorPalette": {
                      "type": "string",
                      "enum": ["black", "gray", "blue"],
                      "default": "black",
                      "description": "Primary color used for links and accents"
                    },
                    "fontFamily": {
                      "type": "string",
                      "enum": ["charter", "lmodern", "times"],
                      "default": "charter",
                      "description": "LaTeX font package to use"
                    },
                    "locale": {
                      "type": "string",
                      "default": "en",
                      "description": "Locale used for i18n strings"
                    },
                    "includeLastUpdated": {
                      "type": "boolean",
                      "default": true,
                      "description": "Whether to render the last-updated footer text"
                    }
                  },
                  "additionalProperties": false
                }
            """.trimIndent(),
        ),
    )

    override fun findAll(): Flux<TemplateMetadata> = Flux.fromIterable(templates)

    override fun findById(id: String): Mono<TemplateMetadata> =
        Mono.justOrEmpty(templates.find { it.id == id })

    override fun existsById(id: String): Mono<Boolean> =
        Mono.just(templates.any { it.id == id })
}
