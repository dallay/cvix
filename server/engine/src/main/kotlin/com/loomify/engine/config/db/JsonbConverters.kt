package com.loomify.engine.config.db

import io.r2dbc.postgresql.codec.Json
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

/**
 * R2DBC converters for PostgreSQL JSONB type support.
 *
 * These converters enable Spring Data R2DBC to map between:
 * - Database JSONB columns -> String (reading)
 * - String -> Database JSONB columns (writing)
 *
 * Required because R2DBC doesn't automatically map String to PostgreSQL JSONB.
 */
@ReadingConverter
class JsonbToStringConverter : Converter<Json, String> {
    override fun convert(source: Json): String = source.asString()
}

@WritingConverter
class StringToJsonbConverter : Converter<String, Json> {
    override fun convert(source: String): Json = Json.of(source)
}
