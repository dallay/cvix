package com.loomify.resume.application.template

import com.loomify.UnitTest
import com.loomify.resume.application.TemplateMetadataResponses
import com.loomify.resume.domain.TemplateMetadata
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

@UnitTest
internal class ListTemplatesQueryHandlerTest {
    private val templateCatalog: TemplateCatalog = mockk()
    private val listTemplatesQueryHandler = ListTemplatesQueryHandler(templateCatalog)

    @Test
    fun `should list templates`() = runTest {
        val query = ListTemplatesQuery(2)
        val templates = listOf(
            TemplateMetadata(
                id = "template1",
                name = "Modern",
                version = "1.0",
                paramsSchema = "{}",
                description = "Modern template",
            ),
            TemplateMetadata(
                id = "template2",
                name = "Classic",
                version = "1.0",
                paramsSchema = "{}",
                description = "Classic template",
            ),
        )
        coEvery { templateCatalog.listTemplates(query.limit) } returns templates

        val result = listTemplatesQueryHandler.handle(query)
        result shouldBe TemplateMetadataResponses.from(templates)
        result.data.size shouldBe 2
        result.data[0].id shouldBe "template1"
        result.data[1].id shouldBe "template2"
        // coVerify on templateCatalog.listTemplates(query.limit)
        coVerify { templateCatalog.listTemplates(query.limit) }
    }
}
