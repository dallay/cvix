package com.cvix.waitlist.infrastructure.persistence.mapper

import com.cvix.common.domain.error.DomainMappingException
import com.cvix.common.domain.vo.email.Email
import com.cvix.waitlist.domain.Language
import com.cvix.waitlist.domain.WaitlistEntry
import com.cvix.waitlist.domain.WaitlistEntryId
import com.cvix.waitlist.domain.WaitlistSource
import com.cvix.waitlist.infrastructure.persistence.entity.WaitlistEntryEntity
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.r2dbc.postgresql.codec.Json
import org.slf4j.LoggerFactory

/**
 * Mapper between WaitlistEntry domain model and WaitlistEntryEntity.
 */
object WaitlistEntryMapper {
    private val logger = LoggerFactory.getLogger(WaitlistEntryMapper::class.java)
    private val objectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Converts a domain WaitlistEntry to a WaitlistEntryEntity.
     *
     * @return The corresponding WaitlistEntryEntity.
     */
    fun WaitlistEntry.toEntity(): WaitlistEntryEntity {
        val metadataJson = this.metadata?.let {
            try {
                Json.of(objectMapper.writeValueAsString(it))
            } catch (e: JsonProcessingException) {
                logger.error("Failed to serialize metadata for waitlist entry {}", this.id, e)
                null
            }
        }
        return WaitlistEntryEntity(
            id = this.id.id,
            email = this.email.value,
            sourceRaw = this.sourceRaw,
            sourceNormalized = this.sourceNormalized.value,
            language = this.language.code,
            ipHash = this.ipHash,
            metadata = metadataJson,
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

        val email = try {
            Email(this.email)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid email value '{}' for waitlist entry {}", this.email, this.id)
            throw DomainMappingException("Invalid email for waitlist entry ${this.id}: ${this.email}", e)
        }

        val language = try {
            Language.fromString(this.language)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid language value '{}' for waitlist entry {}", this.language, this.id)
            throw DomainMappingException("Invalid language for waitlist entry ${this.id}: ${this.language}", e)
        }

        return WaitlistEntry(
            id = WaitlistEntryId(this.id),
            email = email,
            sourceRaw = this.sourceRaw,
            sourceNormalized = WaitlistSource.fromString(this.sourceNormalized),
            language = language,
            ipHash = this.ipHash,
            metadata = metadata,
            createdBy = this.createdBy,
            createdAt = this.createdAt,
            updatedBy = this.updatedBy,
            updatedAt = this.updatedAt,
        )
    }
}
