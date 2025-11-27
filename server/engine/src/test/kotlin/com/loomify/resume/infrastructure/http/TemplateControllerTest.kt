package com.loomify.resume.infrastructure.http

import com.loomify.ControllerTest
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
            .jsonPath("$.data[0].id").isEqualTo("template1")
            .jsonPath("$.data[1].id").isEqualTo("template2")

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
