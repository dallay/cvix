package com.loomify.resume.infrastructure.persistence.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.loomify.resume.domain.Resume
import com.loomify.resume.domain.ResumeDocument
import com.loomify.resume.infrastructure.persistence.entity.ResumeEntity

/**
 * Mapper between domain and infrastructure models.
 *
 * Following Hexagonal Architecture:
 * - Resume: Pure value object (JSON Resume schema data)
 * - ResumeDocument: Domain aggregate root (with identity and metadata)
 * - ResumeEntity: Infrastructure persistence entity (Spring Data, JSONB)
 */
object ResumeMapper {

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    /**
     * Converts ResumeEntity (infrastructure) to ResumeDocument (domain).
     * Deserializes the JSONB data field into Resume and wraps it with metadata.
     */
    fun ResumeEntity.toDomain(): ResumeDocument {
        val resumeContent: Resume = objectMapper.readValue(data)
        return ResumeDocument(
            id = id,
            userId = userId,
            workspaceId = workspaceId,
            title = title,
            content = resumeContent,
            createdAt = createdAt,
            updatedAt = updatedAt ?: createdAt,
            createdBy = createdBy,
            updatedBy = updatedBy ?: createdBy,
        )
    }

    /**
     * Converts ResumeDocument (domain) to ResumeEntity (infrastructure).
     * Serializes the Resume content to JSON for JSONB storage.
     */
    fun ResumeDocument.toEntity(): ResumeEntity {
        return ResumeEntity(
            id = id,
            userId = userId,
            workspaceId = workspaceId,
            title = title,
            data = content.toJsonString(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            createdBy = createdBy,
            updatedBy = updatedBy,
        )
    }

    /**
     * Converts Resume (pure content) to JSON string for JSONB storage.
     */
    fun Resume.toJsonString(): String = objectMapper.writeValueAsString(this)
}
