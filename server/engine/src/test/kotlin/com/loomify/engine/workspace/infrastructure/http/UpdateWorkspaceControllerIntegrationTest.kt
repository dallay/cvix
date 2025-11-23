package com.loomify.engine.workspace.infrastructure.http

import com.loomify.ControllerIntegrationTest
import com.loomify.common.domain.presentation.SimpleMessageResponse
import com.loomify.engine.workspace.WorkspaceStub
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.jdbc.Sql

internal class UpdateWorkspaceControllerIntegrationTest : ControllerIntegrationTest() {

    @Test
    @Sql(
        "/db/user/users.sql",
        "/db/workspace/workspace.sql",
    )
    @Sql(
        "/db/workspace/clean.sql",
        "/db/user/clean.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    )
    fun `should update an workspace`(): Unit = runTest {
        val id = "a0654720-35dc-49d0-b508-1f7df5d915f1"
        val request = WorkspaceStub.generateUpdateRequest()
        webTestClient.mutateWith(csrf()).put()
            .uri("/api/workspace/$id/update")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk.expectBody(SimpleMessageResponse::class.java)
            .isEqualTo(SimpleMessageResponse("Workspace updated successfully."))
    }

    @Test
    @Sql(
        "/db/user/users.sql",
        "/db/workspace/workspace.sql",
    )
    @Sql(
        "/db/workspace/clean.sql",
        "/db/user/clean.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    )
    fun `should return 404 when workspace is not found`(): Unit = runTest {
        val id = "a0654720-35dc-49d0-b508-1f7df5d915f2"
        val request = WorkspaceStub.generateUpdateRequest()
        webTestClient.mutateWith(csrf()).put()
            .uri("/api/workspace/$id/update")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.type").isEqualTo("https://loomify.com/errors/entity-not-found")
            .jsonPath("$.title").isEqualTo("Entity not found")
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.detail").isEqualTo("Workspace not found")
            .jsonPath("$.instance")
            .isEqualTo("/api/workspace/$id/update")
            .jsonPath("$.errorCategory").isEqualTo("NOT_FOUND")
            .jsonPath("$.timestamp").isNotEmpty
    }
}
