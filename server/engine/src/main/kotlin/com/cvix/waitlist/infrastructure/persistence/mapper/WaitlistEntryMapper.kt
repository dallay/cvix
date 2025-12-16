package com.cvix.waitlist.infrastructure.persistence.mapper

import com.cvix.common.domain.vo.email.Email
import com.cvix.waitlist.domain.Language
import com.cvix.waitlist.domain.WaitlistEntry
import com.cvix.waitlist.domain.WaitlistEntryId
import com.cvix.waitlist.domain.WaitlistSource
import com.cvix.waitlist.infrastructure.persistence.entity.WaitlistEntryEntity
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.r2dbc.postgresql.codec.Json
import org.slf4j.LoggerFactory

/**
 * Mapper between WaitlistEntry domain model and WaitlistEntryEntity.
 */
object WaitlistEntryMapper {
    private val logger = LoggerFactory.getLogger(WaitlistEntryMapper::class.java)
    private val objectMapper = ObjectMapper()

    /**
     * Converts a domain WaitlistEntry to a WaitlistEntryEntity.
     *
     * @return The corresponding WaitlistEntryEntity.
     */
    fun WaitlistEntry.toEntity(): WaitlistEntryEntity {
        return WaitlistEntryEntity(
            id = this.id.id,
            email = this.email.value,
            sourceRaw = this.sourceRaw,
            sourceNormalized = this.sourceNormalized.value,
            language = this.language.code,
            ipHash = this.ipHash,
            metadata = this.metadata?.let { Json.of(objectMapper.writeValueAsString(it)) },
            createdBy = this.createdBy,
            createdAt = this.createdAt,
            updatedBy = this.updatedBy,
            updatedAt = this.updatedAt,
        )
    }

    /**
     * Converts a WaitlistEntryEntity to a domain WaitlistEntry.
     *
     * @return The corresponding WaitlistEntry domain model.
     */
    fun WaitlistEntryEntity.toDomain(): WaitlistEntry {
        val metadata: Map<String, Any>? = this.metadata?.let { json ->
            try {
                objectMapper.readValue<Map<String, Any>>(json.asString())
            } catch (e: JsonProcessingException) {
                logger.error("Failed to parse metadata JSON for entry ${this.id}", e)
                null
            }
        }

        return WaitlistEntry(
            id = WaitlistEntryId(this.id),
            email = Email(this.email),
            sourceRaw = this.sourceRaw,
            sourceNormalized = WaitlistSource.fromString(this.sourceNormalized),
            language = Language.fromString(this.language),
            ipHash = this.ipHash,
            metadata = metadata,
            createdBy = this.createdBy,
            createdAt = this.createdAt,
            updatedBy = this.updatedBy,
            updatedAt = this.updatedAt,
        )
    }
}
