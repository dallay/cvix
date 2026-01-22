package com.cvix.subscriber.infrastructure.persistence.converter

import com.cvix.subscriber.domain.Attributes
import io.r2dbc.postgresql.codec.Json
import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue

/**
 * R2DBC converter to read [Attributes] from a PostgreSQL JSONB column.
 *
 * @property jsonMapper The Jackson 3 JsonMapper for deserialization.
 */
@ReadingConverter
class SubscriberAttributesReaderConverter(
    private val jsonMapper: JsonMapper
) : Converter<Json, Attributes> {

    private val log = LoggerFactory.getLogger(SubscriberAttributesReaderConverter::class.java)

    override fun convert(source: Json): Attributes? {
        val jsonString = source.asString()
        if (jsonString.isNullOrBlank()) return null

        return try {
            jsonMapper.readValue<Attributes>(jsonString)
        } catch (e: JacksonException) {
            // Log parsing error and return null — preserve original exception in logs
            log.error("Failed to deserialize Attributes from JSON: $jsonString", e)
            null
        }
    }
}
