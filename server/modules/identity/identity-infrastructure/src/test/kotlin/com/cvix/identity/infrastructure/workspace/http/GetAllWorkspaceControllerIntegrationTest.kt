package com.cvix.identity.infrastructure.workspace.http

import com.cvix.ControllerIntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

internal class GetAllWorkspaceControllerIntegrationTest : ControllerIntegrationTest() {

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
    fun `should get all workspaces`(): Unit = runTest {
        webTestClient
            .get()
            .uri("/api/workspace")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data").isArray
            .jsonPath("$.data.length()").isEqualTo(3)
    }
}
