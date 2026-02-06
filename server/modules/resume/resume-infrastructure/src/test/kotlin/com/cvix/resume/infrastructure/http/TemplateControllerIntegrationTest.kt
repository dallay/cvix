package com.cvix.resume.infrastructure.http

import com.cvix.ControllerIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

internal class TemplateControllerIntegrationTest : ControllerIntegrationTest() {
    private val workspaceId = "a0654720-35dc-49d0-b508-1f7df5d915f1"

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
    fun `should list templates successfully`() {
        webTestClient.get()
            .uri("/api/templates?workspaceId=$workspaceId")
            .header("X-Workspace-Id", workspaceId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data").isArray
            .jsonPath("$.data.length()").isEqualTo(1)
            .jsonPath("$.data[0].id").isEqualTo("engineering")
            .jsonPath("$.data[0].name").isEqualTo("Engineering Resume")
            .jsonPath("$.data[0].version").isEqualTo("1.0.0")
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
    fun `should return 400 when limit is below minimum`() {
        webTestClient.get()
            .uri("/api/templates?workspaceId=$workspaceId&limit=0")
            .header("X-Workspace-Id", workspaceId)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.title").isEqualTo("validation failed")
            .jsonPath("$.detail")
            .isEqualTo("Request parameter validation failed. Please check the provided values.")
            .jsonPath("$.errors").isArray
            .jsonPath("$.errors[0].field").isEqualTo("listTemplates.limit")
            .jsonPath("$.message").isEqualTo("error.validation.failed")
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
    fun `should return 400 when limit is above maximum`() {
        webTestClient.get()
            .uri("/api/templates?workspaceId=$workspaceId&limit=51")
            .header("X-Workspace-Id", workspaceId)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.title").isEqualTo("validation failed")
            .jsonPath("$.detail")
            .isEqualTo("Request parameter validation failed. Please check the provided values.")
            .jsonPath("$.errors").isArray
            .jsonPath("$.errors[0].field").isEqualTo("listTemplates.limit")
            .jsonPath("$.message").isEqualTo("error.validation.failed")
    }
}
