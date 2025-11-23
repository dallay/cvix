package com.loomify.engine.workspace.infrastructure.http

import com.loomify.ControllerTest
import com.loomify.engine.workspace.application.delete.DeleteWorkspaceCommand
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

internal class DeleteWorkspaceControllerTest : ControllerTest() {
    private lateinit var controller: DeleteWorkspaceController
    override lateinit var webTestClient: WebTestClient
    private val id = UUID.randomUUID()
    private val command = DeleteWorkspaceCommand(id)

    @BeforeEach
    override fun setUp() {
        super.setUp()
        controller = DeleteWorkspaceController(mediator)
        webTestClient = WebTestClient.bindToController(controller).build()
        coEvery { mediator.send(command) } returns Unit
    }

    @Test
    fun `should delete workspace`() {
        webTestClient.delete()
            .uri("/api/workspace/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody().isEmpty()

        val commandSlot = slot<DeleteWorkspaceCommand>()
        coVerify(exactly = 1) { mediator.send(capture(commandSlot)) }
        assertEquals(command, commandSlot.captured)
    }
}
