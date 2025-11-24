package com.loomify.resume.infrastructure.http

import com.loomify.ControllerIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.jdbc.Sql

internal class ListResumeControllerIntegrationTest : ControllerIntegrationTest() {
    @Test
    @Sql(
        "/db/user/users.sql",
        "/db/workspace/workspace.sql",
        "/db/resume/resumes.sql",
    )
    @Sql(
        "/db/resume/clean.sql",
        "/db/workspace/clean.sql",
        "/db/user/clean.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    )
    fun `should list resumes successfully`() {
        val workspaceId = "a0654720-35dc-49d0-b508-1f7df5d915f1"

        webTestClient.mutateWith(csrf()).get()
            .uri("/api/resume?workspaceId=$workspaceId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data").isArray
            .jsonPath("$.data.length()").isEqualTo(3)
    }

    @Test
    @Sql(
        "/db/user/users.sql",
        "/db/workspace/workspace.sql",
        "/db/resume/resumes.sql",
    )
    @Sql(
        "/db/resume/clean.sql",
        "/db/workspace/clean.sql",
        "/db/user/clean.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    )
    fun `should list resumes with custom limit`() {
        val workspaceId = "a0654720-35dc-49d0-b508-1f7df5d915f1"

        webTestClient.mutateWith(csrf()).get()
            .uri("/api/resume?workspaceId=$workspaceId&limit=2")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data").isArray
            .jsonPath("$.data.length()").value<Int> { it <= 2 }
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
    fun `should return empty list when no resumes exist`() {
        val workspaceId = "a0654720-35dc-49d0-b508-1f7df5d915f1"

        webTestClient.mutateWith(csrf()).get()
            .uri("/api/resume?workspaceId=$workspaceId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data").isArray
            .jsonPath("$.data.length()").isEqualTo(0)
    }
}
