package com.cvix.resume.infrastructure.http

import com.cvix.ControllerIntegrationTest
import org.junit.jupiter.api.Test

internal class TemplateControllerIntegrationTest : ControllerIntegrationTest() {

    @Test
    fun `should list templates successfully`() {
        webTestClient.get()
            .uri("/api/templates")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data").isArray
            .jsonPath("$.data.length()").isEqualTo(1)
            .jsonPath("$.data[0].id").isEqualTo("engineering")
            .jsonPath("$.data[0].name").isEqualTo("Engineering Resume")
            .jsonPath("$.data[0].version").isEqualTo("0.1.0")
            .jsonPath("$.data[0].description")
            .isEqualTo("Engineering resume template (single-column focused for engineering profiles).")
    }

    @Test
    fun `should return 400 when limit is below minimum`() {
        webTestClient.get()
            .uri("/api/templates?limit=0")
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
    fun `should return 400 when limit is above maximum`() {
        webTestClient.get()
            .uri("/api/templates?limit=51")
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
