package com.cvix.resume.infrastructure.http

import com.cvix.ControllerIntegrationTest
import com.cvix.resume.ResumeTestFixtures
import com.cvix.resume.infrastructure.http.request.UpdateResumeRequest
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.jdbc.Sql

internal class UpdateResumeControllerIntegrationTest : ControllerIntegrationTest() {
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
    fun `should handle updating non-existent resume`() {
        val resumeId = "f47ac10b-58cc-4372-a567-0e02b2c3d479"
        val workspaceId = UUID.fromString("a0654720-35dc-49d0-b508-1f7df5d915f1")
        val request = UpdateResumeRequest(
            title = "Updated Resume Title",
            content = ResumeTestFixtures.createResumeContentRequest(),
            expectedUpdatedAt = null,
        )

        webTestClient.mutateWith(csrf()).put()
            .uri("/api/resume/$resumeId/update")
            .header("X-Workspace-Id", workspaceId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isNotFound
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
    fun `should return 400 for invalid request data`() {
        val resumeId = "f47ac10b-58cc-4372-a567-0e02b2c3d479"
        val workspaceId = UUID.fromString("a0654720-35dc-49d0-b508-1f7df5d915f1")
        val invalidRequest = mapOf("invalid" to "data")

        webTestClient.mutateWith(csrf()).put()
            .uri("/api/resume/$resumeId/update")
            .header("X-Workspace-Id", workspaceId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.title").isEqualTo("Invalid Input")
            .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
            .jsonPath("$.instance").isEqualTo("/api/resume/$resumeId/update")
    }
}
