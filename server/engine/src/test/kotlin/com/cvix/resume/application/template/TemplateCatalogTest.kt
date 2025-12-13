package com.cvix.resume.application.template

import com.cvix.UnitTest
import com.cvix.resume.domain.TemplateMetadata
import com.cvix.resume.domain.TemplateRepository
import com.cvix.resume.domain.TemplateSourceStrategy
import com.cvix.subscription.domain.SubscriptionTier
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@UnitTest
class TemplateCatalogTest {

    private val templateSourceStrategy: TemplateSourceStrategy = mockk()
    private val templateCatalog = TemplateCatalog(templateSourceStrategy)

    @Test
    fun `should filter templates by subscription tier - FREE user sees only FREE templates`() =
        runTest {
            // Given: Repository with templates of different tiers
            val freeTemplate = createTemplate("free-template", SubscriptionTier.FREE)
            val basicTemplate = createTemplate("basic-template", SubscriptionTier.BASIC)
            val professionalTemplate =
                createTemplate("professional-template", SubscriptionTier.PROFESSIONAL)

            val repository = mockk<TemplateRepository>()
            coEvery { repository.findAll() } returns listOf(
                freeTemplate,
                basicTemplate,
                professionalTemplate,
            )
            coEvery { templateSourceStrategy.activeTemplateRepositories(SubscriptionTier.FREE) } returns listOf(
                repository,
            )

            // When: FREE user lists templates
            val result = templateCatalog.listTemplates(SubscriptionTier.FREE, null)

            // Then: Only FREE templates are returned
            result shouldHaveSize 1
            result shouldContain freeTemplate
            result shouldNotContain basicTemplate
            result shouldNotContain professionalTemplate
        }

    @Test
    fun `should filter templates by subscription tier - BASIC user sees FREE and BASIC templates`() =
        runTest {
            // Given: Repository with templates of different tiers
            val freeTemplate = createTemplate("free-template", SubscriptionTier.FREE)
            val basicTemplate = createTemplate("basic-template", SubscriptionTier.BASIC)
            val professionalTemplate =
                createTemplate("professional-template", SubscriptionTier.PROFESSIONAL)

            val repository = mockk<TemplateRepository>()
            coEvery { repository.findAll() } returns listOf(
                freeTemplate,
                basicTemplate,
                professionalTemplate,
            )
            coEvery { templateSourceStrategy.activeTemplateRepositories(SubscriptionTier.BASIC) } returns listOf(
                repository,
            )

            // When: BASIC user lists templates
            val result = templateCatalog.listTemplates(SubscriptionTier.BASIC, null)

            // Then: FREE and BASIC templates are returned
            result shouldHaveSize 2
            result shouldContain freeTemplate
            result shouldContain basicTemplate
            result shouldNotContain professionalTemplate
        }

    @Test
    fun `should filter templates by subscription tier - PROFESSIONAL user sees all templates`() =
        runTest {
            // Given: Repository with templates of different tiers
            val freeTemplate = createTemplate("free-template", SubscriptionTier.FREE)
            val basicTemplate = createTemplate("basic-template", SubscriptionTier.BASIC)
            val professionalTemplate =
                createTemplate("professional-template", SubscriptionTier.PROFESSIONAL)

            val repository = mockk<TemplateRepository>()
            coEvery { repository.findAll() } returns listOf(
                freeTemplate,
                basicTemplate,
                professionalTemplate,
            )
            coEvery { templateSourceStrategy.activeTemplateRepositories(SubscriptionTier.PROFESSIONAL) } returns listOf(
                repository,
            )

            // When: PROFESSIONAL user lists templates
            val result = templateCatalog.listTemplates(SubscriptionTier.PROFESSIONAL, null)

            // Then: All templates are returned
            result shouldHaveSize 3
            result shouldContain freeTemplate
            result shouldContain basicTemplate
            result shouldContain professionalTemplate
        }

    @Test
    fun `should return empty list when no templates match subscription tier`() = runTest {
        // Given: Repository with only PROFESSIONAL templates
        val professionalTemplate =
            createTemplate("professional-template", SubscriptionTier.PROFESSIONAL)

        val repository = mockk<TemplateRepository>()
        coEvery { repository.findAll() } returns listOf(professionalTemplate)
        coEvery { templateSourceStrategy.activeTemplateRepositories(SubscriptionTier.FREE) } returns listOf(
            repository,
        )

        // When: FREE user lists templates
        val result = templateCatalog.listTemplates(SubscriptionTier.FREE, null)

        // Then: Empty list is returned
        result.shouldBeEmpty()
    }

    @Test
    fun `should respect limit parameter`() = runTest {
        // Given: Repository with multiple FREE templates
        val template1 = createTemplate("template-1", SubscriptionTier.FREE)
        val template2 = createTemplate("template-2", SubscriptionTier.FREE)
        val template3 = createTemplate("template-3", SubscriptionTier.FREE)

        val repository = mockk<TemplateRepository>()
        coEvery { repository.findAll() } returns listOf(template1, template2, template3)
        coEvery { templateSourceStrategy.activeTemplateRepositories(SubscriptionTier.FREE) } returns listOf(
            repository,
        )

        // When: User lists templates with limit of 2
        val result = templateCatalog.listTemplates(SubscriptionTier.FREE, 2)

        // Then: Only 2 templates are returned
        result shouldHaveSize 2
    }

    @Test
    fun `should filter first then apply limit`() = runTest {
        // Given: Repository with mixed tier templates
        val freeTemplate1 = createTemplate("free-1", SubscriptionTier.FREE)
        val basicTemplate = createTemplate("basic-1", SubscriptionTier.BASIC)
        val freeTemplate2 = createTemplate("free-2", SubscriptionTier.FREE)
        val freeTemplate3 = createTemplate("free-3", SubscriptionTier.FREE)

        val repository = mockk<TemplateRepository>()
        coEvery { repository.findAll() } returns listOf(
            freeTemplate1,
            basicTemplate,
            freeTemplate2,
            freeTemplate3,
        )
        coEvery { templateSourceStrategy.activeTemplateRepositories(SubscriptionTier.FREE) } returns listOf(
            repository,
        )

        // When: FREE user lists templates with limit of 2
        val result = templateCatalog.listTemplates(SubscriptionTier.FREE, 2)

        // Then: 2 FREE templates are returned (BASIC is filtered out first)
        result shouldHaveSize 2
        result.all { it.requiredSubscriptionTier == SubscriptionTier.FREE } shouldBe true
    }

    private fun createTemplate(id: String, tier: SubscriptionTier): TemplateMetadata {
        return TemplateMetadata(
            id = id,
            name = "Template $id",
            version = "1.0.0",
            templatePath = "classpath:templates/$id",
            requiredSubscriptionTier = tier,
        )
    }
}
