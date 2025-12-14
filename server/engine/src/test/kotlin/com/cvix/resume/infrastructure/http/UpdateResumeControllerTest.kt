package com.cvix.resume.infrastructure.http

import com.cvix.ControllerTest
import com.cvix.resume.ResumeTestFixtures
import com.cvix.resume.application.update.UpdateResumeCommand
import com.cvix.resume.infrastructure.http.mapper.ResumeRequestMapper
import com.cvix.resume.infrastructure.http.request.UpdateResumeRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class UpdateResumeControllerTest : ControllerTest() {
    private val controller = UpdateResumeController(mediator)
    override val webTestClient: WebTestClient = buildWebTestClient(controller)
    private val resumeId = UUID.randomUUID()
    private val workspaceId = UUID.randomUUID()
    private val title = "Updated Resume"
    private lateinit var request: UpdateResumeRequest

    @BeforeEach
    override fun setUp() {
        super.setUp()
        request = UpdateResumeRequest(
            workspaceId = workspaceId,
            title = title,
            content = ResumeTestFixtures.createValidResumeRequestContent(),
            expectedUpdatedAt = Instant.now().toString(),
        )
        coEvery { mediator.send(any<UpdateResumeCommand>()) } returns Unit
    }

    @Test
    fun `should update resume successfully`() {
        webTestClient.put()
            .uri("/api/resume/$resumeId/update")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").exists()

        val commandSlot = slot<UpdateResumeCommand>()
        coVerify(exactly = 1) { mediator.send(capture(commandSlot)) }
        assertEquals(resumeId, commandSlot.captured.id)
        assertEquals(userId, commandSlot.captured.userId)
        assertEquals(workspaceId, commandSlot.captured.workspaceId)
        assertEquals(title, commandSlot.captured.title)
        // Compare mapped Resume objects instead of direct object comparison
        val expectedResume = ResumeRequestMapper.toDomain(request.content)
        assertEquals(expectedResume, commandSlot.captured.content)
        assertEquals(Instant.parse(request.expectedUpdatedAt!!), commandSlot.captured.expectedUpdatedAt)
    }

    @Test
    fun `should return 400 for invalid UUID format`() {
        webTestClient.put()
            .uri("/api/resume/invalid-uuid/update")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should return 400 for invalid request data`() {
        val invalidRequest = mapOf("invalid" to "data")

        webTestClient.put()
            .uri("/api/resume/$resumeId/update")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isBadRequest
    }
}
