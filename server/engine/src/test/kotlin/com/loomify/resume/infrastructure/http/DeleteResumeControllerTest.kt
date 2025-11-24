package com.loomify.resume.infrastructure.http

import com.loomify.ControllerTest
import com.loomify.resume.application.delete.DeleteResumeCommand
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

internal class DeleteResumeControllerTest : ControllerTest() {
    private val controller = DeleteResumeController(mediator)
    override val webTestClient: WebTestClient = buildWebTestClient(controller)
    private val resumeId = UUID.randomUUID()

    @BeforeEach
    override fun setUp() {
        super.setUp()
        coEvery { mediator.send(any<DeleteResumeCommand>()) } returns Unit
    }

    @Test
    fun `should delete resume successfully`() {
        webTestClient.delete()
            .uri("/api/resume/$resumeId")
            .exchange()
            .expectStatus().isNoContent
            .expectBody().isEmpty

        val commandSlot = slot<DeleteResumeCommand>()
        coVerify(exactly = 1) { mediator.send(capture(commandSlot)) }
        assertEquals(resumeId, commandSlot.captured.id)
        assertEquals(userId, commandSlot.captured.userId)
    }

    @Test
    fun `should return 400 for invalid UUID format`() {
        webTestClient.delete()
            .uri("/api/resume/invalid-uuid")
            .exchange()
            .expectStatus().isBadRequest
        coVerify(exactly = 0) { mediator.send(any<DeleteResumeCommand>()) }
    }
}
