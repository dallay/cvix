package com.cvix.resume.infrastructure.persistence.mapper

import com.cvix.resume.domain.Resume
import com.cvix.resume.domain.ResumeDocument
import com.cvix.resume.domain.ResumeDocumentId
import com.cvix.resume.infrastructure.persistence.entity.ResumeEntity
import com.fasterxml.jackson.annotation.JsonInclude
import io.r2dbc.postgresql.codec.Json
import tools.jackson.core.JacksonException
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule

/**
 * Mapper between domain and infrastructure models.
 *
 * Following Hexagonal Architecture:
 * - Resume: Pure value object (JSON Resume schema data)
 * - ResumeDocument: Domain aggregate root (with identity and metadata)
 * - ResumeEntity: Infrastructure persistence entity (Spring Data, JSONB)
 *
 * Jackson 3 Changes:
 * - ObjectMapper replaced by JsonMapper (immutable builder pattern)
 * - Java 8 date/time support built-in (no separate JavaTimeModule needed)
 * - Package changed from com.fasterxml.jackson to tools.jackson
 *   (except annotations which stay in com.fasterxml.jackson.annotation)
 */
object ResumeMapper {

    private val jsonMapper: JsonMapper = jsonMapper {
        // Add Kotlin module for proper Kotlin class support
        addModule(kotlinModule())

        // Don't fail on unknown properties when deserializing
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        // CRITICAL: Write dates as ISO-8601 strings, NOT as arrays
        // Without this, LocalDate serializes as [2018, 9, 5] instead of "2018-09-05"
        // Note: Jackson 3 defaults WRITE_DATES_AS_TIMESTAMPS to false, but explicit is clearer
        disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)

        // Include non-null values only
        changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
    }

    /**
     * Converts ResumeEntity (infrastructure) to ResumeDocument (domain).
     * Deserializes the JSONB data field into Resume and wraps it with metadata.
     */
    fun ResumeEntity.toDomain(): ResumeDocument {
        val resumeContent: Resume = jsonMapper.readValue(data.asString(), Resume::class.java)
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
            id = id.value,
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
            val jsonString = jsonMapper.writeValueAsString(this)
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
