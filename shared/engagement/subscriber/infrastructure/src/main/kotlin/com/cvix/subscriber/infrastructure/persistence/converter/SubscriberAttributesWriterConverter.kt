package com.cvix.subscriber.infrastructure.persistence.converter

import com.cvix.subscriber.domain.Attributes
import com.fasterxml.jackson.core.JsonProcessingException
import io.r2dbc.postgresql.codec.Json
import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import tools.jackson.databind.json.JsonMapper

/**
 * R2DBC converter to write [Attributes] to a PostgreSQL JSONB column.
 *
 * @property jsonMapper The Jackson 3 JsonMapper for serialization.
 */
@WritingConverter
class SubscriberAttributesWriterConverter(
    private val jsonMapper: JsonMapper
) : Converter<Attributes, Json> {

    private val log = LoggerFactory.getLogger(SubscriberAttributesWriterConverter::class.java)

    override fun convert(source: Attributes): Json {
        return try {
            Json.of(jsonMapper.writeValueAsString(source))
        } catch (e: JsonProcessingException) {
            // Log the serialization failure so the original exception isn't silently swallowed
            log.error("Failed to serialize Attributes to JSON", e)
            Json.of("{}")
        }
    }
}
