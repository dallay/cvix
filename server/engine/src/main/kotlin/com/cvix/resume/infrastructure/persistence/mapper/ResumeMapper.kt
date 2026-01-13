package com.cvix.resume.infrastructure.persistence.mapper

import com.cvix.resume.domain.Resume
import com.cvix.resume.domain.ResumeDocument
import com.cvix.resume.domain.ResumeDocumentId
import com.cvix.resume.infrastructure.persistence.entity.ResumeEntity
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.annotation.JsonInclude
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

    private val objectMapper: ObjectMapper = run {
        // jacksonObjectMapper() already registers KotlinModule automatically
        val mapper = jacksonObjectMapper()
        // Register Java 8 date/time module
        mapper.registerModule(JavaTimeModule())
        
        // Don't fail on unknown properties when deserializing
        mapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        
        // CRITICAL: Write dates as ISO-8601 strings, NOT as arrays
        // Without this, LocalDate serializes as [2018, 9, 5] instead of "2018-09-05"
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        
        // Include non-null values only
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        
        mapper
    }

    /**
     * Converts ResumeEntity (infrastructure) to ResumeDocument (domain).
     * Deserializes the JSONB data field into Resume and wraps it with metadata.
     */
    fun ResumeEntity.toDomain(): ResumeDocument {
        val resumeContent: Resume = objectMapper.readValue(data.asString(), Resume::class.java)
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
            Json.of(jsonString)
        } catch (e: JacksonException) {
            throw IllegalStateException(
                "Failed to serialize Resume to JSON: ${e.message}",
                e,
            )
        } catch (e: IllegalArgumentException) {
            throw IllegalStateException("Failed to create JSONB from Resume: ${e.message}", e)
        }
    }
}
