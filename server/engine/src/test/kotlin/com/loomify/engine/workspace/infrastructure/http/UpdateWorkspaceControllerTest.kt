package com.loomify.engine.workspace.infrastructure.http

import com.loomify.ControllerTest
import com.loomify.engine.workspace.WorkspaceStub
import com.loomify.engine.workspace.application.update.UpdateWorkspaceCommand
import com.loomify.engine.workspace.infrastructure.http.request.UpdateWorkspaceRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class UpdateWorkspaceControllerTest : ControllerTest() {
    private val workspace = WorkspaceStub.create()
    private val id = UUID.randomUUID().toString()
    private val command = UpdateWorkspaceCommand(
        id = id,
        name = workspace.name,
        description = workspace.description,
    )
    private val controller = UpdateWorkspaceController(mediator)
    override val webTestClient = buildWebTestClient(controller)

    @BeforeEach
    override fun setUp() {
        super.setUp()
        coEvery { mediator.send(any<UpdateWorkspaceCommand>()) } returns Unit
    }

    @Test
    fun `should return 200 when workspace is updated successfully`() {
        val request = UpdateWorkspaceRequest(
            name = workspace.name,
            description = workspace.description,
        )

        webTestClient.put()
            .uri("/api/workspace/$id/update")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo("Workspace updated successfully")

        val commandSlot = slot<UpdateWorkspaceCommand>()
        coVerify(exactly = 1) { mediator.send(capture(commandSlot)) }
        assertEquals(command, commandSlot.captured)
    }
}
