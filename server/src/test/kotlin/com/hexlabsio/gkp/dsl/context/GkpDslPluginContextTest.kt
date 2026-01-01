package com.hexlabsio.gkp.dsl.context

import com.hexlabsio.gkp.dsl.GkpExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import org.junit.jupiter.api.Test

class GkpDslPluginContextTest {

    @Test
    fun `should create context with project and extension`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)

        // Act
        val context = object : GkpDslPluginContext {
            override val project = mockProject
            override val extension = mockExtension
        }

        // Assert
        context.project shouldBe mockProject
        context.extension shouldBe mockExtension
    }

    @Test
    fun `should maintain project reference`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val projectName = "test-project-name"
        every { mockProject.name } returns projectName
        val mockExtension = mockk<GkpExtension>(relaxed = true)

        // Act
        val context = object : GkpDslPluginContext {
            override val project = mockProject
            override val extension = mockExtension
        }

        // Assert
        context.project.name shouldBe projectName
    }

    @Test
    fun `should maintain extension reference`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)
        val reposDir = "custom-repos-directory"
        every { mockExtension.repositoriesDirectory } returns reposDir

        // Act
        val context = object : GkpDslPluginContext {
            override val project = mockProject
            override val extension = mockExtension
        }

        // Assert
        context.extension.repositoriesDirectory shouldBe reposDir
    }

    @Test
    fun `should allow different implementations of context interface`() {
        // Arrange
        val mockProject1 = mockk<Project>(relaxed = true)
        val mockExtension1 = mockk<GkpExtension>(relaxed = true)
        val mockProject2 = mockk<Project>(relaxed = true)
        val mockExtension2 = mockk<GkpExtension>(relaxed = true)

        // Act
        val context1 = object : GkpDslPluginContext {
            override val project = mockProject1
            override val extension = mockExtension1
        }
        val context2 = object : GkpDslPluginContext {
            override val project = mockProject2
            override val extension = mockExtension2
        }

        // Assert
        context1.project shouldBe mockProject1
        context2.project shouldBe mockProject2
        context1.extension shouldBe mockExtension1
        context2.extension shouldBe mockExtension2
    }

    @Test
    fun `should provide access to project properties through context`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        every { mockProject.name } returns "my-project"
        every { mockProject.version } returns "1.2.3"
        every { mockProject.group } returns "com.hexlabs"
        every { mockProject.path } returns ":app"
        val mockExtension = mockk<GkpExtension>(relaxed = true)

        // Act
        val context = object : GkpDslPluginContext {
            override val project = mockProject
            override val extension = mockExtension
        }

        // Assert
        context.project.name shouldBe "my-project"
        context.project.version shouldBe "1.2.3"
        context.project.group shouldBe "com.hexlabs"
        context.project.path shouldBe ":app"
    }

    @Test
    fun `should provide access to extension configuration through context`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)
        every { mockExtension.repositoriesDirectory } returns "gradle/repos"
        every { mockExtension.cacheDirectory } returns "gradle/cache"

        // Act
        val context = object : GkpDslPluginContext {
            override val project = mockProject
            override val extension = mockExtension
        }

        // Assert
        context.extension.repositoriesDirectory shouldBe "gradle/repos"
        context.extension.cacheDirectory shouldBe "gradle/cache"
    }

    @Test
    fun `should support interface implementation for plugin architecture`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)

        // Act
        val context: GkpDslPluginContext = object : GkpDslPluginContext {
            override val project = mockProject
            override val extension = mockExtension
        }

        // Assert
        context.shouldBeInstanceOf<GkpDslPluginContext>()
    }

    @Test
    fun `should allow context to be passed to plugin apply methods`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)
        val context = object : GkpDslPluginContext {
            override val project = mockProject
            override val extension = mockExtension
        }

        // Act - simulate passing context to a plugin
        fun applyPlugin(ctx: GkpDslPluginContext) {
            ctx.project shouldNotBe null
            ctx.extension shouldNotBe null
        }

        // Assert
        applyPlugin(context)
    }

    @Test
    fun `should support custom context implementations with additional properties`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)

        // Act
        val customContext = object : GkpDslPluginContext {
            override val project = mockProject
            override val extension = mockExtension
            val customProperty = "custom-value"
        }

        // Assert
        customContext.project shouldBe mockProject
        customContext.extension shouldBe mockExtension
        customContext.customProperty shouldBe "custom-value"
    }

    @Test
    fun `should maintain type safety with strongly typed properties`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)

        // Act
        val context = object : GkpDslPluginContext {
            override val project = mockProject
            override val extension = mockExtension
        }

        // Assert
        context.project.shouldBeInstanceOf<Project>()
        context.extension.shouldBeInstanceOf<GkpExtension>()
    }

    @Test
    fun `should support immutable context patterns`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)

        // Act
        val context = object : GkpDslPluginContext {
            override val project = mockProject
            override val extension = mockExtension
        }

        // Assert - properties are vals (immutable)
        val project1 = context.project
        val project2 = context.project
        project1 shouldBe project2
        
        val extension1 = context.extension
        val extension2 = context.extension
        extension1 shouldBe extension2
    }

    @Test
    fun `should enable plugin composition through shared context`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)
        val sharedContext = object : GkpDslPluginContext {
            override val project = mockProject
            override val extension = mockExtension
        }

        // Act - simulate multiple plugins sharing context
        fun plugin1(ctx: GkpDslPluginContext): String {
            return "plugin1-${ctx.project.name}"
        }

        fun plugin2(ctx: GkpDslPluginContext): String {
            return "plugin2-${ctx.project.name}"
        }

        every { mockProject.name } returns "shared"

        // Assert
        plugin1(sharedContext) shouldBe "plugin1-shared"
        plugin2(sharedContext) shouldBe "plugin2-shared"
    }

    @Test
    fun `should support dependency injection pattern for plugin context`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)

        // Act
        class PluginService(private val context: GkpDslPluginContext) {
            fun getProjectName() = context.project.name
            fun getReposDir() = context.extension.repositoriesDirectory
        }

        val context = object : GkpDslPluginContext {
            override val project = mockProject
            override val extension = mockExtension
        }

        every { mockProject.name } returns "injected-project"
        every { mockExtension.repositoriesDirectory } returns "injected-repos"

        val service = PluginService(context)

        // Assert
        service.getProjectName() shouldBe "injected-project"
        service.getReposDir() shouldBe "injected-repos"
    }

    @Test
    fun `should work with nullable project properties`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)
        every { mockProject.description } returns null

        // Act
        val context = object : GkpDslPluginContext {
            override val project = mockProject
            override val extension = mockExtension
        }

        // Assert
        context.project.description shouldBe null
    }

    @Test
    fun `should handle complex extension configurations`() {
        // Arrange
        val mockProject = mockk<Project>(relaxed = true)
        val mockExtension = mockk<GkpExtension>(relaxed = true)
        every { mockExtension.repositoriesDirectory } returns "repos"
        every { mockExtension.cacheDirectory } returns "cache"
        
        // Simulate other extension properties if they exist
        val context = object : GkpDslPluginContext {
            override val project = mockProject
            override val extension = mockExtension
        }

        // Assert
        context.extension shouldBe mockExtension
        context.extension.repositoriesDirectory shouldBe "repos"
        context.extension.cacheDirectory shouldBe "cache"
    }
}