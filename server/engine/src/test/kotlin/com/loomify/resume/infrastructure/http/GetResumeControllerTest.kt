package com.loomify.resume.infrastructure.http

import com.loomify.ControllerTest
import com.loomify.resume.ResumeTestFixtures
import com.loomify.resume.application.ResumeDocumentResponse
import com.loomify.resume.application.get.GetResumeQuery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

internal class GetResumeControllerTest : ControllerTest() {
    private val controller = GetResumeController(mediator)
    override val webTestClient: WebTestClient = buildWebTestClient(controller)
    private val resumeId = UUID.randomUUID()
    private lateinit var response: ResumeDocumentResponse

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val document = ResumeTestFixtures.createResumeDocument(id = resumeId, userId = userId)
        response = ResumeDocumentResponse.from(document)
        coEvery { mediator.send(any<GetResumeQuery>()) } returns response
    }

    @Test
    fun `should get resume successfully`() {
        webTestClient.get()
            .uri("/api/resume/$resumeId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(resumeId.toString())
            .jsonPath("$.userId").isEqualTo(userId.toString())

        val querySlot = slot<GetResumeQuery>()
        coVerify(exactly = 1) { mediator.send(capture(querySlot)) }
        assertEquals(resumeId, querySlot.captured.id)
        assertEquals(userId, querySlot.captured.userId)
    }

    @Test
    fun `should return 400 for invalid UUID format`() {
        webTestClient.get()
            .uri("/api/resume/invalid-uuid")
            .exchange()
            .expectStatus().isBadRequest
        coVerify(exactly = 0) { mediator.send(any<GetResumeQuery>()) }
    }
}
