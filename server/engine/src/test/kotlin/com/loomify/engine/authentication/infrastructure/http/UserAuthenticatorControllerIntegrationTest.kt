package com.loomify.engine.authentication.infrastructure.http

import com.loomify.engine.config.InfrastructureTestContainers
import io.kotest.assertions.print.print
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.web.reactive.server.WebTestClient

private const val ENDPOINT = "/api/auth/login"

private const val TITLE = "User authentication failed"

private const val DETAIL = "Invalid account. User probably hasn't verified email."

private const val ERROR_CATEGORY = "AUTHENTICATION"

@Suppress("MultilineRawStringIndentation")
@AutoConfigureWebTestClient
internal class UserAuthenticatorControllerIntegrationTest : InfrastructureTestContainers() {
    // this user is created by default in Keycloak container (see demo-realm-test.json)
    private val email = "john.doe@loomify.com"
    private val username = "john.doe"
    private val password = "S3cr3tP@ssw0rd*123"

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `should not authenticate a user without csrf token`() {
        webTestClient.post()
            .uri(ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "email": "$email",
                    "password": "$password"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should authenticate a user by email`() {
        webTestClient
            .mutateWith(csrf())
            .post()
            .uri(ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "email": "$email",
                    "password": "$password"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.token").isNotEmpty
            .jsonPath("$.expiresIn").isNotEmpty
            .jsonPath("$.refreshToken").isNotEmpty
            .jsonPath("$.refreshExpiresIn").isNotEmpty
            .jsonPath("$.tokenType").isNotEmpty
            .jsonPath("$.notBeforePolicy").isNotEmpty
            .jsonPath("$.sessionState").isNotEmpty
            .jsonPath("$.scope").isNotEmpty
            .consumeWith {
                println(it.responseBody?.print())
            }
    }

    @Test
    fun `should not authenticate with invalid email format`() {
        webTestClient
            .mutateWith(csrf())
            .post()
            .uri(ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "email": "$username",
                    "password": "$password"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.title").isEqualTo("validation failed")
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.detail").isEqualTo("Request validation failed. Please check the provided data.")
    }

    @Test
    fun `should not authenticate a user with invalid credentials`() {
        webTestClient
            .mutateWith(csrf())
            .post()
            .uri(ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "email": "$email",
                    "password": "${password}invalidPassword"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .jsonPath("$.title").isEqualTo(TITLE)
            .jsonPath("$.detail").isEqualTo(DETAIL)
            .jsonPath("$.instance").isEqualTo(ENDPOINT)
            .jsonPath("$.errorCategory").isEqualTo(ERROR_CATEGORY)
            .jsonPath("$.timestamp").isNotEmpty
    }

    @Test
    fun `should not authenticate a user with invalid username`() {
        webTestClient
            .mutateWith(csrf())
            .post()
            .uri(ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "email": "invalid.user@loomify.com",
                    "password": "$password"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .jsonPath("$.title").isEqualTo(TITLE)
            .jsonPath("$.detail").isEqualTo(DETAIL)
            .jsonPath("$.instance").isEqualTo(ENDPOINT)
            .jsonPath("$.errorCategory").isEqualTo(ERROR_CATEGORY)
            .jsonPath("$.timestamp").isNotEmpty
    }
}
