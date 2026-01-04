package com.cvix.resume.infrastructure.http

import com.cvix.ControllerTest
import com.cvix.resume.ResumeTestFixtures.createResumeDocument
import com.cvix.resume.ResumeTestFixtures.createResumeRequest
import com.cvix.resume.application.ResumeDocumentResponse
import com.cvix.resume.application.create.CreateResumeCommand
import com.cvix.resume.infrastructure.http.mapper.ResumeRequestMapper
import com.cvix.resume.infrastructure.http.request.CreateResumeRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

internal class CreateResumeControllerTest : ControllerTest() {
    private val controller = CreateResumeController(mediator)
    override val webTestClient: WebTestClient = buildWebTestClient(controller)
    private lateinit var command: CreateResumeCommand
    private lateinit var request: CreateResumeRequest
    private val resumeId = UUID.randomUUID()
    private val title = "My Resume"

    @BeforeEach
    override fun setUp() {
        super.setUp()
        request = createResumeRequest(title = title)
        val resume = ResumeRequestMapper.toDomain(request.content)
        command = CreateResumeCommand(
            resumeId,
            userId,
            workspaceId,
            title = title,
            content = resume,
            createdBy = userId.toString(),
        )
        val mockDocument = createResumeDocument(
            id = resumeId,
            userId = userId,
            workspaceId = workspaceId,
            title = title,
            content = resume,
        )
        val mockResponse = ResumeDocumentResponse.from(mockDocument)
        coEvery { mediator.send(any<CreateResumeCommand>()) } returns mockResponse
    }

    @Test
    fun `should create a new resume`() {

        webTestClient.put()
            .uri("/api/resume/$resumeId")
            .header("X-Workspace-Id", workspaceId.toString())
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isEqualTo(resumeId.toString())
            .jsonPath("$.title").isEqualTo(title)
        val commandSlot = slot<CreateResumeCommand>()
        coVerify(exactly = 1) { mediator.send(capture(commandSlot)) }
        assertEquals(resumeId, commandSlot.captured.id)
        assertEquals(userId, commandSlot.captured.userId)
        assertEquals(workspaceId, commandSlot.captured.workspaceId)
    }
}
