package com.cvix.resume.application.template

import com.cvix.FixtureDataLoader
import com.cvix.UnitTest
import com.cvix.resume.application.TemplateMetadataResponses
import com.cvix.resume.domain.Locale
import com.cvix.resume.domain.TemplateMetadata
import com.cvix.subscription.domain.SubscriptionResolver
import com.cvix.subscription.domain.SubscriptionTier
import com.cvix.workspace.WorkspaceAuthorizationService
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.UUID
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

@UnitTest
internal class ListTemplatesQueryHandlerTest {
    private val templateCatalog: TemplateCatalog = mockk()
    private val workspaceAuthorizationService: WorkspaceAuthorizationService = mockk()
    private val subscriptionResolver: SubscriptionResolver = mockk()
    private val listTemplatesQueryHandler = ListTemplatesQueryHandler(
        templateCatalog,
        workspaceAuthorizationService,
        subscriptionResolver,
    )

    @Test
    fun `should list templates for free user`() = runTest {
        // Given
        val userId = UUID.randomUUID()
        val workspaceId = UUID.randomUUID()
        val query = ListTemplatesQuery(userId, workspaceId, 2)
        val engineering: TemplateMetadata =
            FixtureDataLoader.fromResource("data/json/resume/template/metadata/engineering.json")
        val modern: TemplateMetadata =
            FixtureDataLoader.fromResource("data/json/resume/template/metadata/modern.json")
        val templates = listOf(engineering, modern)

        coEvery { workspaceAuthorizationService.ensureAccess(workspaceId, userId) } returns Unit
        coEvery { subscriptionResolver.resolve(userId.toString()) } returns SubscriptionTier.FREE
        coEvery { templateCatalog.listTemplates(SubscriptionTier.FREE, query.limit) } returns templates

        // When
        val result = listTemplatesQueryHandler.handle(query)

        // Then
        result shouldBe TemplateMetadataResponses.from(templates)
        result.data.size shouldBe 2
        result.data[0].id shouldBe "engineering"
        result.data[0].name shouldBe "Engineering Resume"
        result.data[0].version shouldBe "0.1.0"
        result.data[0].supportedLocales shouldBe listOf(Locale.EN, Locale.ES)
        result.data[0].params shouldBe engineering.params
        result.data[0].previewUrl shouldBe "https://placehold.co/300x600.png"

        result.data[1].id shouldBe "modern"
        result.data[1].name shouldBe "Modern Resume"
        result.data[1].version shouldBe "0.1.0"
        result.data[1].supportedLocales shouldBe listOf(Locale.EN, Locale.ES)
        result.data[1].params shouldBe modern.params
        result.data[1].previewUrl shouldBe "https://placehold.co/300x600.png"

        coVerify {
            workspaceAuthorizationService.ensureAccess(workspaceId, userId)
            subscriptionResolver.resolve(userId.toString())
            templateCatalog.listTemplates(SubscriptionTier.FREE, query.limit)
        }
    }

    @Test
    fun `should resolve user subscription tier correctly`() = runTest {
        // Given
        val userId = UUID.randomUUID()
        val workspaceId = UUID.randomUUID()
        val query = ListTemplatesQuery(userId, workspaceId, 10)

        coEvery { workspaceAuthorizationService.ensureAccess(workspaceId, userId) } returns Unit
        coEvery { subscriptionResolver.resolve(userId.toString()) } returns SubscriptionTier.BASIC
        coEvery { templateCatalog.listTemplates(SubscriptionTier.BASIC, query.limit) } returns emptyList()

        // When
        listTemplatesQueryHandler.handle(query)

        // Then
        coVerify {
            subscriptionResolver.resolve(userId.toString())
            templateCatalog.listTemplates(SubscriptionTier.BASIC, query.limit)
        }
    }
}
