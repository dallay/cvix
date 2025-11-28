package com.loomify.resume.infrastructure.http

import com.loomify.ControllerTest
import com.loomify.FixtureDataLoader
import com.loomify.resume.application.TemplateMetadataResponse
import com.loomify.resume.application.TemplateMetadataResponses
import com.loomify.resume.application.template.ListTemplatesQuery
import com.loomify.resume.domain.TemplateMetadata
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

internal class TemplateControllerTest : ControllerTest() {
    private val controller = TemplateController(mediator)
    override val webTestClient: WebTestClient = buildWebTestClient(controller)
    private lateinit var response: TemplateMetadataResponses

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val engineering: TemplateMetadata =
            FixtureDataLoader.fromResource("data/json/template-metadata/engineering.json")
        val modern: TemplateMetadata =
            FixtureDataLoader.fromResource("data/json/template-metadata/modern.json")
        val templates = listOf(engineering, modern)
        response = TemplateMetadataResponses(
            data = templates.map { TemplateMetadataResponse.from(it) },
        )
        coEvery { mediator.send(any<ListTemplatesQuery>()) } returns response
    }

    @Test
    fun `should list templates successfully`() {
        webTestClient.get()
            .uri("/api/templates")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data").isArray
            .jsonPath("$.data.length()").isEqualTo(2)
            .jsonPath("$.data[0].id").isEqualTo("engineering")
            .jsonPath("$.data[0].name").isEqualTo("Engineering Resume")
            .jsonPath("$.data[0].version").isEqualTo("0.1.0")
            .jsonPath("$.data[0].description")
            .isEqualTo("Engineering resume template (single-column focused for engineering profiles).")
            .jsonPath("$.data[0].supportedLocales").isArray
            .jsonPath("$.data[0].supportedLocales.length()").isEqualTo(2)
            .jsonPath("$.data[0].supportedLocales[0]").isEqualTo("EN")
            .jsonPath("$.data[0].supportedLocales[1]").isEqualTo("ES")
            .jsonPath("$.data[0].previewUrl").isEqualTo("https://placehold.co/300x600.png")
            .jsonPath("$.data[0].params.colorPalette").isEqualTo("blue")
            .jsonPath("$.data[0].params.fontFamily").isEqualTo("Roboto")
            .jsonPath("$.data[0].params.spacing").isEqualTo("normal")
            .jsonPath("$.data[0].params.density").isEqualTo("comfortable")
            .jsonPath("$.data[0].params.customParams.includePhoto").isEqualTo(true)
            .jsonPath("$.data[0].params.customParams.highlightSkills").isEqualTo(true)
            .jsonPath("$.data[1].id").isEqualTo("modern")
            .jsonPath("$.data[1].name").isEqualTo("Modern Resume")
            .jsonPath("$.data[1].version").isEqualTo("0.1.0")
            .jsonPath("$.data[1].description")
            .isEqualTo("Modern resume template (clean and professional design for various profiles).")
            .jsonPath("$.data[1].supportedLocales").isArray
            .jsonPath("$.data[1].supportedLocales.length()").isEqualTo(2)
            .jsonPath("$.data[1].supportedLocales[0]").isEqualTo("EN")
            .jsonPath("$.data[1].supportedLocales[1]").isEqualTo("ES")
            .jsonPath("$.data[1].previewUrl").isEqualTo("https://placehold.co/300x600.png")
            .jsonPath("$.data[1].params.colorPalette").isEqualTo("blue")
            .jsonPath("$.data[1].params.fontFamily").isEqualTo("Roboto")
            .jsonPath("$.data[1].params.spacing").isEqualTo("normal")
            .jsonPath("$.data[1].params.density").isEqualTo("comfortable")
            .jsonPath("$.data[1].params.customParams.includePhoto").isEqualTo(true)
            .jsonPath("$.data[1].params.customParams.highlightSkills").isEqualTo(true)

        val querySlot = slot<ListTemplatesQuery>()
        coVerify(exactly = 1) { mediator.send(capture(querySlot)) }
        // Default limit is 50 in controller
        assertEquals(50, querySlot.captured.limit)
    }

    @Test
    fun `should handle internal error`() {
        coEvery { mediator.send(any<ListTemplatesQuery>()) } throws RuntimeException("Internal error")
        webTestClient.get()
            .uri("/api/templates")
            .exchange()
            .expectStatus().is5xxServerError
    }
}
