package com.cvix.resume.infrastructure.template.source

import com.cvix.UnitTest
import com.cvix.resume.domain.TemplateMetadata
import com.cvix.resume.domain.TemplateMetadataLoader
import io.kotest.common.runBlocking
import io.mockk.coEvery
import io.mockk.mockk
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertNull

@UnitTest
internal class FilesystemTemplateSourceRepositoryTest {
    @Test
    @Suppress("UNCHECKED_CAST")
    fun `loads templates from filesystem directory and allows lookup and existence checks`() {
        val tempDir = Files.createTempDirectory("templates-load").toFile()
        try {
            val tmpl = File(tempDir, "tmplA")
            tmpl.mkdirs()
            File(tmpl, "metadata.yaml").writeText("id: tmpl-a\nname: TmplA\nversion: v1")

            val properties =
                TemplateSourceProperties(source = TemplateSourceProperties.SourceConfig(path = tempDir.absolutePath))
            val loader = mockk<TemplateMetadataLoader>()
            coEvery { loader.loadTemplateMetadata(any(), any()) } returns TemplateMetadata(
                id = "tmpl-a",
                name = "TmplA",
                version = "v1",
                templatePath = tmpl.absolutePath,
            )

            val repo = FilesystemTemplateSourceRepository(properties, loader)
            val all = runBlocking { repo.findAll() }
            assertEquals(1, all.size)

            val found = runBlocking { repo.findById("tmpl-a") }
            assertEquals("TmplA", found!!.name)

            val exists = runBlocking { repo.existsById("tmpl-a") }
            assertEquals(true, exists)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `returns empty list when base path does not exist`() {
        val properties =
            TemplateSourceProperties(source = TemplateSourceProperties.SourceConfig(path = "/no/such/path/exists-xyz"))
        val loader = mockk<TemplateMetadataLoader>()
        val repo = FilesystemTemplateSourceRepository(properties, loader)

        val all = runBlocking { repo.findAll() }
        assertEquals(0, all.size)

        val found = runBlocking { repo.findById("any") }
        assertNull(found)
    }

    @Test
    fun `returns empty list when base path is a file not directory`() {
        val tempFile = Files.createTempFile("templates-file", ".tmp").toFile()
        try {
            val properties =
                TemplateSourceProperties(source = TemplateSourceProperties.SourceConfig(path = tempFile.absolutePath))
            val loader = mockk<TemplateMetadataLoader>()
            val repo = FilesystemTemplateSourceRepository(properties, loader)

            val all = runBlocking { repo.findAll() }
            assertEquals(0, all.size)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `skips metadata files that fail to load and returns others`() {
        val tempDir = Files.createTempDirectory("templates-skip").toFile()
        try {
            val good = File(tempDir, "good")
            val bad = File(tempDir, "bad")
            good.mkdirs()
            bad.mkdirs()
            File(good, "metadata.yaml").writeText("good")
            File(bad, "metadata.yaml").writeText("bad")

            val properties =
                TemplateSourceProperties(source = TemplateSourceProperties.SourceConfig(path = tempDir.absolutePath))
            val loader = mockk<TemplateMetadataLoader>()

            coEvery {
                loader.loadTemplateMetadata(
                    any(),
                    match { it.contains("good") },
                )
            } returns TemplateMetadata(
                id = "good-id",
                name = "Good",
                version = "1",
                templatePath = good.absolutePath,
            )
            coEvery {
                loader.loadTemplateMetadata(
                    any(),
                    match { it.contains("bad") },
                )
            } coAnswers { throw IllegalArgumentException("invalid metadata") }

            val repo = FilesystemTemplateSourceRepository(properties, loader)
            val all = runBlocking { repo.findAll() }
            assertEquals(1, all.size)
            assertEquals("good-id", all.first().id)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `findById returns null for unknown id and existsById false`() {
        val tempDir = Files.createTempDirectory("templates-empty").toFile()
        try {
            val properties =
                TemplateSourceProperties(source = TemplateSourceProperties.SourceConfig(path = tempDir.absolutePath))
            val loader = mockk<TemplateMetadataLoader>()
            val repo = FilesystemTemplateSourceRepository(properties, loader)

            val found = runBlocking { repo.findById("non-existent") }
            assertNull(found)
            val exists = runBlocking { repo.existsById("non-existent") }
            assertEquals(false, exists)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `handles exception during directory walk and returns empty list`() {
        val tempDir = Files.createTempDirectory("templates-exception").toFile()
        try {
            val sub = File(tempDir, "t")
            sub.mkdirs()
            File(sub, "metadata.yaml").writeText("x")

            val properties =
                TemplateSourceProperties(source = TemplateSourceProperties.SourceConfig(path = tempDir.absolutePath))
            val loader = mockk<TemplateMetadataLoader>()
            coEvery {
                loader.loadTemplateMetadata(
                    any(),
                    any(),
                )
            } coAnswers { @Suppress("TooGenericExceptionThrown") throw RuntimeException("boom") }

            val repo = FilesystemTemplateSourceRepository(properties, loader)
            val all = runBlocking { repo.findAll() }
            assertEquals(0, all.size)
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
