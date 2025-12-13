package com.cvix.resume.infrastructure.template.source

import com.cvix.UnitTest
import com.cvix.resume.domain.TemplateMetadata
import com.cvix.resume.domain.TemplateMetadataLoader
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.io.Resource
import org.springframework.core.io.support.ResourcePatternResolver

@UnitTest
internal class ClasspathTemplateSourceRepositoryTest {

    @Test
    fun `should load templates from classpath resources and allow lookup by id`() {
        val resolver = mockk<ResourcePatternResolver>()
        val loader = mockk<TemplateMetadataLoader>()

        val res1 = mockk<Resource>()
        val res2 = mockk<Resource>()

        every { res1.filename } returns "meta1.yaml"
        every { res1.description } returns "meta1"
        every { res1.inputStream } returns ByteArrayInputStream("content1".toByteArray())

        every { res2.filename } returns "meta2.yaml"
        every { res2.description } returns "meta2"
        every { res2.inputStream } returns ByteArrayInputStream("content2".toByteArray())

        every { resolver.getResources(any()) } returns arrayOf(res1, res2)

        runBlocking {
            coEvery { loader.loadTemplateMetadata(any(), "meta1.yaml") } returns TemplateMetadata(
                id = "id1",
                name = "Name 1",
                version = "v1",
                templatePath = "path1",
            )
            coEvery { loader.loadTemplateMetadata(any(), "meta2.yaml") } returns TemplateMetadata(
                id = "id2",
                name = "Name 2",
                version = "v2",
                templatePath = "path2",
            )
        }

        val repo = ClasspathTemplateSourceRepository(resolver, loader)

        val all = runBlocking { repo.findAll() }
        assertEquals(2, all.size)
        assertTrue(all.any { it.id == "id1" })
        assertTrue(all.any { it.id == "id2" })

        val found = runBlocking { repo.findById("id1") }
        assertEquals("Name 1", found?.name)

        val missing = runBlocking { repo.findById("no-such") }
        assertNull(missing)
    }

    @Test
    fun `should return empty list when no metadata resources are found`() {
        val resolver = mockk<ResourcePatternResolver>()
        val loader = mockk<TemplateMetadataLoader>()

        every { resolver.getResources(any()) } returns emptyArray()

        val repo = ClasspathTemplateSourceRepository(resolver, loader)

        val all = runBlocking { repo.findAll() }
        assertEquals(0, all.size)
    }

    @Test
    fun `should skip resources that fail to load and continue processing others`() {
        val resolver = mockk<ResourcePatternResolver>()
        val loader = mockk<TemplateMetadataLoader>()

        val good = mockk<Resource>()
        val bad = mockk<Resource>()

        every { good.filename } returns "good.yaml"
        every { good.description } returns "good"
        every { good.inputStream } returns ByteArrayInputStream("ok".toByteArray())

        every { bad.filename } returns "bad.yaml"
        every { bad.description } returns "bad"
        every { bad.inputStream } returns ByteArrayInputStream("bad".toByteArray())

        every { resolver.getResources(any()) } returns arrayOf(bad, good)

        runBlocking {
            coEvery { loader.loadTemplateMetadata(any(), "good.yaml") } returns TemplateMetadata(
                id = "good-id",
                name = "Good",
                version = "1",
                templatePath = "p",
            )
            coEvery {
                loader.loadTemplateMetadata(
                    any(),
                    "bad.yaml",
                )
            } coAnswers { throw IllegalArgumentException("invalid") }
        }

        val repo = ClasspathTemplateSourceRepository(resolver, loader)

        val all = runBlocking { repo.findAll() }
        assertEquals(1, all.size)
        assertEquals("good-id", all.first().id)
    }

    @Test
    fun `should return true if template exists and false if not`() {
        val resolver = mockk<ResourcePatternResolver>()
        val loader = mockk<TemplateMetadataLoader>()

        val res1 = mockk<Resource>()
        val res2 = mockk<Resource>()

        every { res1.filename } returns "meta1.yaml"
        every { res1.description } returns "meta1"
        every { res1.inputStream } returns ByteArrayInputStream("content1".toByteArray())

        every { res2.filename } returns "meta2.yaml"
        every { res2.description } returns "meta2"
        every { res2.inputStream } returns ByteArrayInputStream("content2".toByteArray())

        every { resolver.getResources(any()) } returns arrayOf(res1, res2)

        runBlocking {
            coEvery { loader.loadTemplateMetadata(any(), "meta1.yaml") } returns TemplateMetadata(
                id = "id1",
                name = "Name 1",
                version = "v1",
                templatePath = "path1",
            )
            coEvery { loader.loadTemplateMetadata(any(), "meta2.yaml") } returns TemplateMetadata(
                id = "id2",
                name = "Name 2",
                version = "v2",
                templatePath = "path2",
            )
        }

        val repo = ClasspathTemplateSourceRepository(resolver, loader)

        val exists1 = runBlocking { repo.existsById("id1") }
        val exists2 = runBlocking { repo.existsById("id2") }
        val existsMissing = runBlocking { repo.existsById("no-such") }

        assertTrue(exists1)
        assertTrue(exists2)
        assertTrue(!existsMissing)
    }
}
