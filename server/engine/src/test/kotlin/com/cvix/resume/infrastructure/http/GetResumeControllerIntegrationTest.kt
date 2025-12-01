package com.cvix.resume.infrastructure.http

import com.cvix.ControllerIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.jdbc.Sql

internal class GetResumeControllerIntegrationTest : ControllerIntegrationTest() {
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
    fun `should return 404 when getting non-existent resume`() {
        val resumeId = "f47ac10b-58cc-4372-a567-0e02b2c3d479"

        webTestClient.mutateWith(csrf()).get()
            .uri("/api/resume/$resumeId")
            .exchange()
            .expectStatus().isNotFound
    }
}
