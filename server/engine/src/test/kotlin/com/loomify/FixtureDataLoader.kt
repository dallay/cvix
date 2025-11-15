package com.loomify

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.io.InputStream

/**
 * Test-only JSON/text loader utilities.
 * - Supports loading from classpath resources, files, or raw strings
 * - Works with any target type using reified generics (including collections)
 * - Configured for Kotlin + Java time types and ignores unknown properties by default
 */
object FixtureDataLoader {
    @PublishedApi
    internal val mapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    /**
     * Load and deserialize JSON from a classpath resource.
     * Example: fromResource<List<MyType>>("data/my-type.json")
     */
    internal inline fun <reified T> fromResource(
        jsonPath: String,
        classLoader: ClassLoader? = Thread.currentThread().contextClassLoader,
    ): T {
        val cl = classLoader ?: this::class.java.classLoader
        val inputStream = cl?.getResourceAsStream(jsonPath)
            ?: throw IllegalArgumentException("Resource not found: $jsonPath")
        return useStream(inputStream)
    }
    fun readResource(path: String): String {
        val cl = Thread.currentThread().contextClassLoader
            ?: FixtureDataLoader::class.java.classLoader
        val stream = cl.getResourceAsStream(path)
            ?: throw IllegalArgumentException("Resource not found: $path")
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
    /**
     * Load and deserialize JSON from a file system path.
     */
    @Suppress("unused")
    internal inline fun <reified T> fromFile(path: String): T =
        File(path)
            .inputStream()
            .use { mapper.readValue(it, typeRef<T>()) }

    /**
     * Deserialize JSON from a raw JSON string.
     */
    @Suppress("unused")
    internal inline fun <reified T> fromString(json: String): T =
        mapper.readValue(json, typeRef<T>())

    @PublishedApi
    internal inline fun <reified T> useStream(input: InputStream): T =
        input.use { mapper.readValue(it, typeRef<T>()) }

    @PublishedApi
    internal inline fun <reified T> typeRef(): TypeReference<T> = object : TypeReference<T>() {}
}
