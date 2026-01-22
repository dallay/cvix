package com.cvix.subscriber.infrastructure.persistence.converter

import com.cvix.UnitTest
import com.cvix.subscriber.domain.Attributes
import io.r2dbc.postgresql.codec.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule

@UnitTest
internal class SubscriberAttributesConverterTest {

    private val jsonMapper = jsonMapper {
        addModule(kotlinModule())
    }
    private val reader = SubscriberAttributesReaderConverter(jsonMapper)
    private val writer = SubscriberAttributesWriterConverter(jsonMapper)

    @Test
    fun `should convert Attributes to Json and back`() {
        // Arrange
        val attributes = Attributes(
            tags = listOf("tag1", "tag2"),
            metadata = mapOf("key" to "value"),
        )

        // Act
        val json = writer.convert(attributes)
        val result = reader.convert(json)

        // Assert
        assertNotNull(result)
        assertEquals(attributes, result)
    }

    @Test
    fun `should handle empty attributes`() {
        // Arrange
        val attributes = Attributes()

        // Act
        val json = writer.convert(attributes)
        val result = reader.convert(json)

        // Assert
        assertNotNull(result)
        assertEquals(attributes, result)
    }

    @Test
    fun `reader should return null on invalid json`() {
        // Arrange
        val invalidJson = Json.of("invalid")

        // Act
        val result = reader.convert(invalidJson)

        // Assert
        assertEquals(null, result)
    }
}
