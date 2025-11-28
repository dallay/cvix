package com.loomify.resume.application.template

import com.loomify.FixtureDataLoader
import com.loomify.UnitTest
import com.loomify.resume.application.TemplateMetadataResponses
import com.loomify.resume.domain.Locale
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
        val engineering: TemplateMetadata =
            FixtureDataLoader.fromResource("data/json/template-metadata/engineering.json")
        val modern: TemplateMetadata =
            FixtureDataLoader.fromResource("data/json/template-metadata/modern.json")
        val templates = listOf(engineering, modern)
        coEvery { templateCatalog.listTemplates(query.limit) } returns templates

        val result = listTemplatesQueryHandler.handle(query)
        result shouldBe TemplateMetadataResponses.from(templates)
        result.data.size shouldBe 2
        result.data[0].id shouldBe "engineering"
        result.data[0].name shouldBe "Engineering Resume"
        result.data[0].version shouldBe "0.1.0"
        result.data[0].description shouldBe
            "Engineering resume template (single-column focused for engineering profiles)."
        result.data[0].supportedLocales shouldBe listOf(Locale.EN, Locale.ES)
        result.data[0].params shouldBe engineering.params
        result.data[0].previewUrl shouldBe
            "https://placehold.co/300x600.png"

        result.data[1].id shouldBe "modern"
        result.data[1].name shouldBe "Modern Resume"
        result.data[1].version shouldBe "0.1.0"
        result.data[1].description shouldBe
            "Modern resume template (clean and professional design for various profiles)."
        result.data[1].supportedLocales shouldBe listOf(Locale.EN, Locale.ES)
        result.data[1].params shouldBe modern.params
        result.data[1].previewUrl shouldBe
            "https://placehold.co/300x600.png"

        coVerify { templateCatalog.listTemplates(query.limit) }
    }
}
