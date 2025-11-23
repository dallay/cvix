package com.loomify.resume.infrastructure.persistence.mapper

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.loomify.resume.domain.Resume
import com.loomify.resume.domain.ResumeDocument
import com.loomify.resume.domain.ResumeDocumentId
import com.loomify.resume.infrastructure.persistence.entity.ResumeEntity
import io.r2dbc.postgresql.codec.Json

/**
 * Mapper between domain and infrastructure models.
 *
 * Following Hexagonal Architecture:
 * - Resume: Pure value object (JSON Resume schema data)
 * - ResumeDocument: Domain aggregate root (with identity and metadata)
 * - ResumeEntity: Infrastructure persistence entity (Spring Data, JSONB)
 */
object ResumeMapper {

    private val objectMapper: ObjectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
        // Configure for proper JSON serialization
        findAndRegisterModules()
        // Don't fail on unknown properties when deserializing
        configure(
            com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false,
        )
        // Include non-null values only
        setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    }

    /**
     * Converts ResumeEntity (infrastructure) to ResumeDocument (domain).
     * Deserializes the JSONB data field into Resume and wraps it with metadata.
     */
    fun ResumeEntity.toDomain(): ResumeDocument {
        val resumeContent: Resume = objectMapper.readValue(data.asString())
        return ResumeDocument(
            id = ResumeDocumentId(id),
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
            id = id.id,
            userId = userId,
            workspaceId = workspaceId,
            title = title,
            data = content.toJson(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            createdBy = createdBy,
            updatedBy = updatedBy,
        )
    }

    /**
     * Converts Resume (pure content) to Json type for JSONB storage.
     */
    fun Resume.toJson(): Json {
        return try {
            val jsonString = objectMapper.writeValueAsString(this)
            return Json.of(jsonString)
        } catch (e: JsonMappingException) {
            throw IllegalStateException(
                "Invalid Resume structure for JSON serialization: ${e.message}",
                e,
            )
        } catch (e: JsonProcessingException) {
            throw IllegalStateException("Failed to serialize Resume to JSON: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw IllegalStateException("Failed to create JSONB from Resume: ${e.message}", e)
        }
    }
}
