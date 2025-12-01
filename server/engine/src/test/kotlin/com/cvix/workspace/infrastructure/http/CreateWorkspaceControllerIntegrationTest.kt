package com.cvix.workspace.infrastructure.http

import com.cvix.ControllerIntegrationTest
import com.cvix.workspace.WorkspaceStub
import com.cvix.workspace.infrastructure.http.request.CreateWorkspaceRequest
import java.util.*
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.jdbc.Sql

private const val ENDPOINT = "/api/workspace"

internal class CreateWorkspaceControllerIntegrationTest : ControllerIntegrationTest() {
    @Test
    @Sql(
        "/db/user/users.sql",
    )
    @Sql(
        "/db/user/clean.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    )
    fun `should create a new workspace`() {
        val ownerId = UUID.fromString("efc4b2b8-08be-4020-93d5-f795762bf5c9")
        val request: CreateWorkspaceRequest = WorkspaceStub.generateRequest(ownerId = ownerId)
        val id = UUID.randomUUID().toString()
        webTestClient.mutateWith(csrf()).put()
            .uri("$ENDPOINT/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody().isEmpty
    }

    @Test
    @Sql(
        "/db/user/users.sql",
        "/db/workspace/all-workspaces.sql",
    )
    @Sql(
        "/db/workspace/clean.sql",
        "/db/user/clean.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    )
    fun `should fail when the workspace already exists`() {
        val request: CreateWorkspaceRequest = WorkspaceStub.generateRequest()
        val id = "95ded4bb-2946-4dbe-87df-afb701788eb4"
        webTestClient.mutateWith(csrf()).put()
            .uri("$ENDPOINT/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.type").isEqualTo("https://cvix.com/errors/bad-request")
            .jsonPath("$.title").isEqualTo("Bad request")
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.detail").isEqualTo("Error creating workspace")
            .jsonPath("$.instance").isEqualTo("$ENDPOINT/$id")
            .jsonPath("$.errorCategory").isEqualTo("BAD_REQUEST")
            .jsonPath("$.timestamp").isNumber
    }
}
