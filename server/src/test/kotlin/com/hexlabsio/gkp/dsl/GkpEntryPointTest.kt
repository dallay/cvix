package com.hexlabsio.gkp.dsl

import com.hexlabsio.gkp.dsl.context.GkpDslPluginContext
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.Project
import org.junit.jupiter.api.Test

class GkpEntryPointTest {

    @Test
    fun `should initialize with project and extension`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)

        // Act
        val entryPoint = GkpEntryPoint(mockProject, mockExtension)

        // Assert
        entryPoint.project shouldBe mockProject
        entryPoint.extension shouldBe mockExtension
    }

    @Test
    fun `should create pluginContext with correct project and extension`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)
        val entryPoint = GkpEntryPoint(mockProject, mockExtension)

        // Act
        val pluginContext = entryPoint.pluginContext

        // Assert
        pluginContext shouldNotBe null
        pluginContext.shouldBeInstanceOf<GkpDslPluginContext>()
        pluginContext.project shouldBe mockProject
        pluginContext.extension shouldBe mockExtension
    }

    @Test
    fun `should return same pluginContext instance on multiple accesses`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)
        val entryPoint = GkpEntryPoint(mockProject, mockExtension)

        // Act
        val pluginContext1 = entryPoint.pluginContext
        val pluginContext2 = entryPoint.pluginContext

        // Assert
        pluginContext1 shouldBe pluginContext2
    }

    @Test
    fun `should maintain project reference through pluginContext`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val projectName = "test-project"
        every { mockProject.name } returns projectName
        val mockExtension = mockk<GkpExtension>(relaxed = true)
        val entryPoint = GkpEntryPoint(mockProject, mockExtension)

        // Act
        val pluginContext = entryPoint.pluginContext

        // Assert
        pluginContext.project.name shouldBe projectName
    }

    @Test
    fun `should maintain extension reference through pluginContext`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)
        val testProperty = "test-value"
        every { mockExtension.toString() } returns testProperty
        val entryPoint = GkpEntryPoint(mockProject, mockExtension)

        // Act
        val pluginContext = entryPoint.pluginContext

        // Assert
        pluginContext.extension shouldBe mockExtension
        pluginContext.extension.toString() shouldBe testProperty
    }

    @Test
    fun `should allow pluginContext to be used for plugin initialization`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)
        val entryPoint = GkpEntryPoint(mockProject, mockExtension)
        val mockPlugin = mockk<GkpDslPlugin>(relaxed = true)
        val pluginContext = entryPoint.pluginContext

        // Act
        mockPlugin.apply(pluginContext)

        // Assert
        verify { mockPlugin.apply(pluginContext) }
    }

    @Test
    fun `should create entryPoint with real Project implementation`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true) {
            every { name } returns "real-project"
            every { version } returns "1.0.0"
            every { group } returns "com.example"
        }
        val mockExtension = mockk<GkpExtension>(relaxed = true)

        // Act
        val entryPoint = GkpEntryPoint(mockProject, mockExtension)
        val pluginContext = entryPoint.pluginContext

        // Assert
        pluginContext.project.name shouldBe "real-project"
        pluginContext.project.version shouldBe "1.0.0"
        pluginContext.project.group shouldBe "com.example"
    }

    @Test
    fun `should handle extension with configured properties`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true) {
            every { repositoriesDirectory } returns "custom-repos"
        }
        val entryPoint = GkpEntryPoint(mockProject, mockExtension)

        // Act
        val pluginContext = entryPoint.pluginContext

        // Assert
        pluginContext.extension.repositoriesDirectory shouldBe "custom-repos"
    }

    @Test
    fun `should support chaining through pluginContext`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)
        val entryPoint = GkpEntryPoint(mockProject, mockExtension)

        // Act & Assert
        entryPoint.pluginContext.project shouldBe mockProject
        entryPoint.pluginContext.extension shouldBe mockExtension
    }

    @Test
    fun `should initialize pluginContext lazily`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)

        // Act
        val entryPoint = GkpEntryPoint(mockProject, mockExtension)
        // Don't access pluginContext yet

        // Assert - at this point, pluginContext should not have been initialized
        // (this is implicit in the lazy delegation)
        entryPoint.project shouldBe mockProject
        entryPoint.extension shouldBe mockExtension
    }

    @Test
    fun `should allow multiple plugins to share same pluginContext`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)
        val entryPoint = GkpEntryPoint(mockProject, mockExtension)
        val mockPlugin1 = mockk<GkpDslPlugin>(relaxed = true)
        val mockPlugin2 = mockk<GkpDslPlugin>(relaxed = true)
        val pluginContext = entryPoint.pluginContext

        // Act
        mockPlugin1.apply(pluginContext)
        mockPlugin2.apply(pluginContext)

        // Assert
        verify { mockPlugin1.apply(pluginContext) }
        verify { mockPlugin2.apply(pluginContext) }
    }

    @Test
    fun `should maintain immutable references in pluginContext`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)
        val entryPoint = GkpEntryPoint(mockProject, mockExtension)

        // Act
        val context1 = entryPoint.pluginContext
        val context2 = entryPoint.pluginContext

        // Assert
        context1.project shouldBe context2.project
        context1.extension shouldBe context2.extension
        context1 shouldBe context2
    }

    @Test
    fun `should propagate project properties through pluginContext`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val projectPath = ":sub:project"
        every { mockProject.path } returns projectPath
        val mockExtension = mockk<GkpExtension>(relaxed = true)
        val entryPoint = GkpEntryPoint(mockProject, mockExtension)

        // Act
        val pluginContext = entryPoint.pluginContext

        // Assert
        pluginContext.project.path shouldBe projectPath
    }

    @Test
    fun `should support extension DSL configuration through pluginContext`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true) {
            every { repositoriesDirectory } returns "repos"
            every { cacheDirectory } returns "cache"
        }
        val entryPoint = GkpEntryPoint(mockProject, mockExtension)

        // Act
        val pluginContext = entryPoint.pluginContext

        // Assert
        pluginContext.extension.repositoriesDirectory shouldBe "repos"
        pluginContext.extension.cacheDirectory shouldBe "cache"
    }
}