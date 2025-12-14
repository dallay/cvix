package com.cvix.resume.infrastructure.http

import com.cvix.ControllerTest
import com.cvix.resume.ResumeTestFixtures.createResumeRequest
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
    private val workspaceId = UUID.randomUUID()
    private val title = "My Resume"

    @BeforeEach
    override fun setUp() {
        super.setUp()
        request = createResumeRequest(workspaceId = workspaceId, title = title)
        val resume = ResumeRequestMapper.toDomain(request.content)
        command = CreateResumeCommand(
            resumeId,
            userId,
            workspaceId,
            title = title,
            content = resume,
            createdBy = userId.toString(),
        )
        coEvery { mediator.send(any<CreateResumeCommand>()) } returns Unit
    }

    @Test
    fun `should create a new resume`() {

        webTestClient.put()
            .uri("/api/resume/$resumeId")
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody().isEmpty
        val commandSlot = slot<CreateResumeCommand>()
        coVerify(exactly = 1) { mediator.send(capture(commandSlot)) }
        assertEquals(resumeId, commandSlot.captured.id)
        assertEquals(userId, commandSlot.captured.userId)
        assertEquals(workspaceId, commandSlot.captured.workspaceId)
    }
}
