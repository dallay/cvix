package com.loomify.workspace.infrastructure.http

import com.loomify.ControllerIntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.jdbc.Sql

private const val ENDPOINT = "/api/workspace"

internal class DeleteWorkspaceControllerIntegrationTest : ControllerIntegrationTest() {

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
    fun `should delete workspace`(): Unit = runTest {
        val id = "a0654720-35dc-49d0-b508-1f7df5d915f1"
        webTestClient.mutateWith(csrf()).delete()
            .uri("$ENDPOINT/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody().isEmpty
    }

    @Test
    fun `should return OK when an workspace is not found`(): Unit = runTest {
        val id = "94be1a32-cf2e-4dfc-892d-bdd8ac7ad354"
        webTestClient.mutateWith(csrf()).delete()
            .uri("$ENDPOINT/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody().isEmpty
    }
}
