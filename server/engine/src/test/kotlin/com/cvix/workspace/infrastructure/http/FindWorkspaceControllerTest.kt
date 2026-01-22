package com.cvix.workspace.infrastructure.http

import com.cvix.ControllerTest
import com.cvix.workspace.WorkspaceStub
import com.cvix.workspace.application.WorkspaceResponse
import com.cvix.workspace.application.find.FindWorkspaceQuery
import com.cvix.workspace.domain.WorkspaceNotFoundException
import io.mockk.coEvery
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

internal class FindWorkspaceControllerTest : ControllerTest() {
    private var controller: FindWorkspaceController = FindWorkspaceController(mediator)
    override val webTestClient: WebTestClient = buildWebTestClient(controller)
    private val id = UUID.randomUUID()

    @BeforeEach
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun `should return workspace when workspace is found`() {
        val workspace = WorkspaceStub.create()
        val query = FindWorkspaceQuery(id)
        val response = WorkspaceResponse.from(workspace)
        coEvery { mediator.send(query) } returns response

        webTestClient.get()
            .uri("/api/workspace/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody(WorkspaceResponse::class.java)
            .isEqualTo(response)
    }

    @Test
    fun `should return 404 when workspace is not found`() {
        val query = FindWorkspaceQuery(id)
        coEvery { mediator.send(query) } throws WorkspaceNotFoundException("Workspace not found")

        webTestClient.get()
            .uri("/api/workspace/$id")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should return 400 for invalid workspace ID format`() {
        val invalidId = "invalid-uuid"

        webTestClient.get()
            .uri("/api/workspace/$invalidId")
            .exchange()
            .expectStatus().isBadRequest
    }
}
