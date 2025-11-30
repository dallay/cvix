package com.cvix.resume.infrastructure.http

import com.cvix.ControllerTest
import com.cvix.resume.ResumeTestFixtures
import com.cvix.resume.application.ResumeDocumentResponse
import com.cvix.resume.application.ResumeDocumentResponses
import com.cvix.resume.application.list.ListResumesQuery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

internal class ListResumeControllerTest : ControllerTest() {
    private val controller = ListResumeController(mediator)
    override val webTestClient: WebTestClient = buildWebTestClient(controller)
    private val workspaceId = UUID.randomUUID()
    private lateinit var response: ResumeDocumentResponses

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val document1 = ResumeTestFixtures.createResumeDocument(userId = userId, workspaceId = workspaceId)
        val document2 = ResumeTestFixtures.createResumeDocument(userId = userId, workspaceId = workspaceId)
        response = ResumeDocumentResponses(
            data = listOf(
                ResumeDocumentResponse.from(document1),
                ResumeDocumentResponse.from(document2),
            ),
        )
        coEvery { mediator.send(any<ListResumesQuery>()) } returns response
    }

    @Test
    fun `should list resumes successfully`() {
        webTestClient.get()
            .uri("/api/resume?workspaceId=$workspaceId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data").isArray
            .jsonPath("$.data.length()").isEqualTo(2)

        val querySlot = slot<ListResumesQuery>()
        coVerify(exactly = 1) { mediator.send(capture(querySlot)) }
        assertEquals(userId, querySlot.captured.userId)
        assertEquals(workspaceId, querySlot.captured.workspaceId)
        assertEquals(50, querySlot.captured.limit)
        assertEquals(null, querySlot.captured.cursor)
    }

    @Test
    fun `should list resumes with custom limit`() {
        webTestClient.get()
            .uri("/api/resume?workspaceId=$workspaceId&limit=10")
            .exchange()
            .expectStatus().isOk

        val querySlot = slot<ListResumesQuery>()
        coVerify(exactly = 1) { mediator.send(capture(querySlot)) }
        assertEquals(10, querySlot.captured.limit)
    }

    @Test
    fun `should list resumes with cursor for pagination`() {
        val cursor = UUID.randomUUID()

        webTestClient.get()
            .uri("/api/resume?workspaceId=$workspaceId&cursor=$cursor")
            .exchange()
            .expectStatus().isOk

        val querySlot = slot<ListResumesQuery>()
        coVerify(exactly = 1) { mediator.send(capture(querySlot)) }
        assertEquals(cursor, querySlot.captured.cursor)
    }

    @Test
    fun `should return 400 when workspaceId is missing`() {
        webTestClient.get()
            .uri("/api/resume")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should return 400 when limit exceeds maximum`() {
        webTestClient.get()
            .uri("/api/resume?workspaceId=$workspaceId&limit=101")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should return 400 when limit is less than minimum`() {
        webTestClient.get()
            .uri("/api/resume?workspaceId=$workspaceId&limit=0")
            .exchange()
            .expectStatus().isBadRequest
    }
}
