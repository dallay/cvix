package com.cvix.resume.infrastructure.persistence.mapper

import com.cvix.resume.domain.Resume
import com.cvix.resume.domain.ResumeDocument
import com.cvix.resume.domain.ResumeDocumentId
import com.cvix.resume.infrastructure.persistence.entity.ResumeEntity
import tools.jackson.core.JsonProcessingException
import tools.jackson.databind.JsonMappingException
import tools.jackson.databind.ObjectMapper
import tools.jackson.datatype.jsr310.JavaTimeModule
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
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
            tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false,
        )
        // CRITICAL: Write dates as ISO-8601 strings, NOT as arrays
        // Without this, LocalDate serializes as [2018, 9, 5] instead of "2018-09-05"
        configure(
            tools.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
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
            Json.of(jsonString)
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
