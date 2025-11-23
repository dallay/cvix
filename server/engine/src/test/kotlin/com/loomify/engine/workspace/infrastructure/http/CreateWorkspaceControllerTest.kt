package com.loomify.engine.workspace.infrastructure.http

import com.loomify.ControllerTest
import com.loomify.engine.workspace.WorkspaceStub
import com.loomify.engine.workspace.application.create.CreateWorkspaceCommand
import com.loomify.engine.workspace.infrastructure.http.request.CreateWorkspaceRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CreateWorkspaceControllerTest : ControllerTest() {
    private val workspace = WorkspaceStub.create()
    private val id = UUID.randomUUID().toString()
    private val command = CreateWorkspaceCommand(
        id = id,
        name = workspace.name,
        description = workspace.description,
        ownerId = workspace.ownerId.id.toString(),
    )
    private val controller = CreateWorkspaceController(mediator)
    override val webTestClient = buildWebTestClient(controller)

    @BeforeEach
    override fun setUp() {
        super.setUp()
        coEvery { mediator.send(eq(command)) } returns Unit
    }

    @Test
    fun `should create a new workspace`() {
        val request = CreateWorkspaceRequest(
            name = workspace.name,
            description = workspace.description,
            ownerId = workspace.ownerId.id.toString(),
        )
        webTestClient.put()
            .uri("/api/workspace/$id")
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody().isEmpty

        val commandSlot = slot<CreateWorkspaceCommand>()
        coVerify(exactly = 1) { mediator.send(capture(commandSlot)) }
        assertEquals(command, commandSlot.captured)
    }
}
