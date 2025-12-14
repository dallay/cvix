package com.cvix.resume.application.template

import com.cvix.UnitTest
import com.cvix.resume.domain.TemplateMetadata
import com.cvix.resume.domain.TemplateRepository
import com.cvix.resume.domain.TemplateSourceStrategy
import com.cvix.resume.domain.exception.TemplateAccessDeniedException
import com.cvix.resume.domain.exception.TemplateNotFoundException
import com.cvix.subscription.domain.SubscriptionTier
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@UnitTest
class TemplateFinderTest {

    private val templateSourceStrategy: TemplateSourceStrategy = mockk()
    private val templateFinder = TemplateFinder(templateSourceStrategy)

    @Test
    fun `should find and grant access to FREE template for FREE subscription tier`() = runTest {
        // Given
        val templateId = "free-template"
        val userId = UUID.randomUUID()
        val template = createTemplate(templateId, SubscriptionTier.FREE)
        val mockRepository: TemplateRepository = mockk()

        coEvery { templateSourceStrategy.activeTemplateRepositories(SubscriptionTier.FREE) } returns listOf(
            mockRepository,
        )
        coEvery { mockRepository.findById(templateId) } returns template

        // When
        val result =
            templateFinder.findByIdAndValidateAccess(templateId, userId, SubscriptionTier.FREE)

        // Then
        result shouldBe template
    }

    @Test
    fun `should find FREE template when searching multiple repositories`() = runTest {
        // Given
        val templateId = "free-template"
        val userId = UUID.randomUUID()
        val template = createTemplate(templateId, SubscriptionTier.FREE)
        val repo1: TemplateRepository = mockk()
        val repo2: TemplateRepository = mockk()

        coEvery { templateSourceStrategy.activeTemplateRepositories(SubscriptionTier.BASIC) } returns listOf(
            repo1,
            repo2,
        )
        coEvery { repo1.findById(templateId) } returns null
        coEvery { repo2.findById(templateId) } returns template

        // When
        val result =
            templateFinder.findByIdAndValidateAccess(templateId, userId, SubscriptionTier.BASIC)

        // Then
        result shouldBe template
    }

    @Test
    fun `should find and grant access to BASIC template for BASIC subscriber`() = runTest {
        // Given
        val templateId = "basic-template"
        val userId = UUID.randomUUID()
        val template = createTemplate(templateId, SubscriptionTier.BASIC)
        val mockRepository: TemplateRepository = mockk()

        coEvery { templateSourceStrategy.activeTemplateRepositories(SubscriptionTier.BASIC) } returns listOf(
            mockRepository,
        )
        coEvery { mockRepository.findById(templateId) } returns template

        // When
        val result =
            templateFinder.findByIdAndValidateAccess(templateId, userId, SubscriptionTier.BASIC)

        // Then
        result shouldBe template
    }

    @Test
    fun `should find and grant access to BASIC template for PROFESSIONAL subscriber`() = runTest {
        // Given
        val templateId = "basic-template"
        val userId = UUID.randomUUID()
        val template = createTemplate(templateId, SubscriptionTier.BASIC)
        val mockRepository: TemplateRepository = mockk()

        coEvery {
            templateSourceStrategy.activeTemplateRepositories(SubscriptionTier.PROFESSIONAL)
        } returns listOf(mockRepository)
        coEvery { mockRepository.findById(templateId) } returns template

        // When
        val result = templateFinder.findByIdAndValidateAccess(
            templateId,
            userId,
            SubscriptionTier.PROFESSIONAL,
        )

        // Then
        result shouldBe template
    }

    @Test
    fun `should deny access to BASIC template for FREE subscription tier`() = runTest {
        // Given
        val templateId = "basic-template"
        val userId = UUID.randomUUID()
        val template = createTemplate(templateId, SubscriptionTier.BASIC)
        val mockRepository: TemplateRepository = mockk()

        coEvery { templateSourceStrategy.activeTemplateRepositories(SubscriptionTier.FREE) } returns listOf(
            mockRepository,
        )
        coEvery { mockRepository.findById(templateId) } returns template

        // When & Then
        shouldThrow<TemplateAccessDeniedException> {
            templateFinder.findByIdAndValidateAccess(templateId, userId, SubscriptionTier.FREE)
        }
    }

    @Test
    fun `should deny access to PROFESSIONAL template for BASIC subscriber`() = runTest {
        // Given
        val templateId = "professional-template"
        val userId = UUID.randomUUID()
        val template = createTemplate(templateId, SubscriptionTier.PROFESSIONAL)
        val mockRepository: TemplateRepository = mockk()

        coEvery { templateSourceStrategy.activeTemplateRepositories(SubscriptionTier.BASIC) } returns listOf(
            mockRepository,
        )
        coEvery { mockRepository.findById(templateId) } returns template

        // When & Then
        shouldThrow<TemplateAccessDeniedException> {
            templateFinder.findByIdAndValidateAccess(templateId, userId, SubscriptionTier.BASIC)
        }
    }

    @Test
    fun `should throw TemplateNotFoundException when template not found in any repository`() =
        runTest {
            // Given
            val templateId = "non-existent-template"
            val userId = UUID.randomUUID()
            val repo1: TemplateRepository = mockk()
            val repo2: TemplateRepository = mockk()

            coEvery { templateSourceStrategy.activeTemplateRepositories(SubscriptionTier.FREE) } returns listOf(
                repo1,
                repo2,
            )
            coEvery { repo1.findById(templateId) } returns null
            coEvery { repo2.findById(templateId) } returns null

            // When & Then
            shouldThrow<TemplateNotFoundException> {
                templateFinder.findByIdAndValidateAccess(templateId, userId, SubscriptionTier.FREE)
            }
        }

    private fun createTemplate(
        id: String,
        requiredTier: SubscriptionTier
    ): TemplateMetadata {
        return TemplateMetadata(
            id = id,
            name = "Test Template",
            version = "1.0.0",
            templatePath = "classpath:templates/$id",
            requiredSubscriptionTier = requiredTier,
        )
    }
}
