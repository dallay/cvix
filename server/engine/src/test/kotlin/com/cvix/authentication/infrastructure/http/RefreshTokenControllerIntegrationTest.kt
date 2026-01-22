package com.cvix.authentication.infrastructure.http

import com.cvix.authentication.infrastructure.cookie.AuthCookieBuilder
import com.cvix.common.domain.authentication.AccessToken
import com.cvix.config.InfrastructureTestContainers
import io.kotest.assertions.print.print
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

private const val ENDPOINT = "/api/auth/refresh-token"

@Suppress("MultilineRawStringIndentation")
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
internal class RefreshTokenControllerIntegrationTest : InfrastructureTestContainers() {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    private val webTestClient: WebTestClient by lazy {
        WebTestClient.bindToApplicationContext(applicationContext)
            .apply(springSecurity())
            .configureClient()
            .build()
    }

    private val email = "john.doe@profiletailors.com"
    private val password = "S3cr3tP@ssw0rd*123"
    private var accessToken: AccessToken? = null

    @BeforeEach
    fun setUp() {
        accessToken = webTestClient
            .mutateWith(csrf())
            .post()
            .uri("/api/auth/login")
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
            .expectBody(AccessToken::class.java)
            .returnResult()
            .responseBody
    }

    @Test
    fun `should refresh token`() {
        webTestClient
            .mutateWith(csrf())
            .post()
            .uri(ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(AuthCookieBuilder.REFRESH_TOKEN, accessToken?.refreshToken ?: "")
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
}
