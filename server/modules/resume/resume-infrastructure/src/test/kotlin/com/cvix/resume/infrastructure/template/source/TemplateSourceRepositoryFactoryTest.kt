package com.cvix.resume.infrastructure.template.source

import com.cvix.UnitTest
import com.cvix.resume.domain.TemplateRepository
import com.cvix.resume.domain.TemplateSourceKeys
import com.cvix.resume.domain.TemplateSourceType
import com.cvix.resume.infrastructure.template.source.TemplateSourceProperties.SourceConfig
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@UnitTest
internal class TemplateSourceRepositoryFactoryTest {

    @Test
    fun `should return repositories in configured priority order`() {
        val filesystemRepo: TemplateRepository = mockk()
        val classpathRepo: TemplateRepository = mockk()

        val sources = mapOf(
            TemplateSourceKeys.FILESYSTEM to filesystemRepo,
            TemplateSourceKeys.CLASSPATH to classpathRepo,
        )

        val properties = TemplateSourceProperties(
            source = SourceConfig(
                types = listOf(
                    TemplateSourceType.FILESYSTEM,
                    TemplateSourceType.CLASSPATH,
                ),
            ),
        )

        val factory = TemplateSourceRepositoryFactory(sources, properties)

        val repositories = runBlocking { factory.activeTemplateRepositories() }

        assertEquals(2, repositories.size)
        assertSame(filesystemRepo, repositories[0])
        assertSame(classpathRepo, repositories[1])
    }

    @Test
    fun `should throw when any configured source is missing`() {
        val classpathRepo: TemplateRepository = mockk()

        val sources = mapOf(
            TemplateSourceKeys.CLASSPATH to classpathRepo,
        )

        val properties = TemplateSourceProperties(
            source = SourceConfig(
                types = listOf(
                    TemplateSourceType.FILESYSTEM,
                    TemplateSourceType.CLASSPATH,
                ),
            ),
        )

        val factory = TemplateSourceRepositoryFactory(sources, properties)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            runBlocking { factory.activeTemplateRepositories() }
        }
        assertTrue(exception.message!!.contains("FILESYSTEM"))
        assertTrue(exception.message!!.contains(TemplateSourceKeys.FILESYSTEM))
    }

    @Test
    fun `should throw when no configured repositories are available`() {
        val sources = emptyMap<String, TemplateRepository>()

        val properties = TemplateSourceProperties(
            source = SourceConfig(types = listOf(TemplateSourceType.FILESYSTEM)),
        )

        val factory = TemplateSourceRepositoryFactory(sources, properties)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { factory.activeTemplateRepositories() }
        }
    }

    @Test
    fun `should default to classpath when configured types are empty`() {
        val classpathRepo: TemplateRepository = mockk()

        val sources = mapOf(TemplateSourceKeys.CLASSPATH to classpathRepo)

        val properties = TemplateSourceProperties(
            source = SourceConfig(types = emptyList()),
        )

        val factory = TemplateSourceRepositoryFactory(sources, properties)

        val repositories = runBlocking { factory.activeTemplateRepositories() }

        assertEquals(1, repositories.size)
        assertSame(classpathRepo, repositories[0])
    }

    @Test
    fun `get should return repository by type and availableSources should list keys`() {
        val classpathRepo: TemplateRepository = mockk()
        val sources = mapOf(TemplateSourceKeys.CLASSPATH to classpathRepo)
        val properties = TemplateSourceProperties()
        val factory = TemplateSourceRepositoryFactory(sources, properties)

        val found = factory.get(TemplateSourceType.CLASSPATH)
        assertSame(classpathRepo, found)
        // availableSources should contain the key we provided
        assertTrue(factory.availableSources().contains(TemplateSourceKeys.CLASSPATH))
    }

    @Test
    fun `should throw when no repository exists for type using get`() {
        val classpathRepo: TemplateRepository = mockk()
        val sources = mapOf(TemplateSourceKeys.CLASSPATH to classpathRepo)
        val properties = TemplateSourceProperties()
        val factory = TemplateSourceRepositoryFactory(sources, properties)

        assertThrows(IllegalArgumentException::class.java) {
            factory.get(TemplateSourceType.FILESYSTEM)
        }
    }

    @Test
    fun `should throw when no repository exists for TemplateSourceType`() {
        val classpathRepo: TemplateRepository = mockk()
        val sources = mapOf(TemplateSourceKeys.CLASSPATH to classpathRepo)
        val properties = TemplateSourceProperties()
        val factory = TemplateSourceRepositoryFactory(sources, properties)

        assertThrows(IllegalArgumentException::class.java) {
            factory.get(TemplateSourceType.FILESYSTEM)
        }
    }

    @Test
    fun `get should throw when requested source type is unsupported`() {
        val sources = emptyMap<String, TemplateRepository>()
        val properties = TemplateSourceProperties()
        val factory = TemplateSourceRepositoryFactory(sources, properties)

        assertThrows(IllegalArgumentException::class.java) {
            factory.get(TemplateSourceType.CLASSPATH)
        }
    }
}
