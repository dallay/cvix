package com.cvix.resume.infrastructure.template.source

import com.cvix.UnitTest
import com.cvix.resume.domain.TemplateRepository
import com.cvix.resume.domain.TemplateSourceKeys
import com.cvix.resume.domain.TemplateSourceType
import com.cvix.resume.infrastructure.template.source.TemplateSourceProperties.SourceConfig
import com.cvix.subscription.domain.SubscriptionTier
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

        val repositories = runBlocking { factory.activeTemplateRepositories(SubscriptionTier.FREE) }

        assertEquals(2, repositories.size)
        assertSame(filesystemRepo, repositories[0])
        assertSame(classpathRepo, repositories[1])
    }

    @Test
    fun `should skip missing configured source and return available repositories`() {
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

        val repositories =
            runBlocking { factory.activeTemplateRepositories(SubscriptionTier.BASIC) }

        assertEquals(1, repositories.size)
        assertSame(classpathRepo, repositories[0])
    }

    @Test
    fun `should throw when no configured repositories are available`() {
        val sources = emptyMap<String, TemplateRepository>()

        val properties = TemplateSourceProperties(
            source = SourceConfig(types = listOf(TemplateSourceType.FILESYSTEM)),
        )

        val factory = TemplateSourceRepositoryFactory(sources, properties)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { factory.activeTemplateRepositories(SubscriptionTier.FREE) }
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

        val repositories = runBlocking { factory.activeTemplateRepositories(SubscriptionTier.FREE) }

        assertEquals(1, repositories.size)
        assertSame(classpathRepo, repositories[0])
    }

    @Test
    fun `get should return repository by bean name and availableSources should list keys`() {
        val classpathRepo: TemplateRepository = mockk()
        val sources = mapOf(TemplateSourceKeys.CLASSPATH to classpathRepo)
        val properties = TemplateSourceProperties()
        val factory = TemplateSourceRepositoryFactory(sources, properties)

        val found = factory.get(TemplateSourceKeys.CLASSPATH)
        assertSame(classpathRepo, found)
        // availableSources should contain the key we provided
        assertTrue(factory.availableSources().contains(TemplateSourceKeys.CLASSPATH))
    }

    @Test
    fun `get should throw when requested source is unsupported`() {
        val sources = emptyMap<String, TemplateRepository>()
        val properties = TemplateSourceProperties()
        val factory = TemplateSourceRepositoryFactory(sources, properties)

        assertThrows(IllegalArgumentException::class.java) {
            factory.get("non-existent")
        }
    }
}
