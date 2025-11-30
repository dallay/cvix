package com.cvix.authentication.infrastructure.http

import com.cvix.ControllerIntegrationTest
import com.cvix.authentication.domain.AccessToken
import com.cvix.authentication.infrastructure.cookie.AuthCookieBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.web.reactive.server.returnResult

private const val ENDPOINT = "/api/auth/logout"

@Suppress("MultilineRawStringIndentation")
internal class UserLogoutControllerIntegrationTest : ControllerIntegrationTest() {

    private val email = "john.doe@loomify.com"
    private val password = "S3cr3tP@ssw0rd*123"
    private var accessToken: AccessToken? = null

    @BeforeEach
    override fun setUp() {
        super.setUp()
        startInfrastructure()
        val returnResult = webTestClient
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
            .returnResult<AccessToken>()
        accessToken = returnResult.responseBody.blockFirst()
    }

    @Test
    fun `logout user successfully`() {

        webTestClient.mutateWith(csrf()).post()
            .uri(ENDPOINT)
            .cookie(AuthCookieBuilder.ACCESS_TOKEN, accessToken?.token ?: "")
            .cookie(AuthCookieBuilder.REFRESH_TOKEN, accessToken?.refreshToken ?: "")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `logout user with missing cookies`() {

        webTestClient.mutateWith(csrf()).post()
            .uri(ENDPOINT)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `logout user with missing refresh token`() {

        webTestClient.mutateWith(csrf()).post()
            .uri(ENDPOINT)
            .cookie(AuthCookieBuilder.ACCESS_TOKEN, accessToken?.token ?: "")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `logout user with wrong refresh token`() {

        webTestClient.mutateWith(csrf()).post()
            .uri(ENDPOINT)
            .cookie(AuthCookieBuilder.ACCESS_TOKEN, accessToken?.token ?: "")
            .cookie(AuthCookieBuilder.REFRESH_TOKEN, "wrong")
            .exchange()
            .expectStatus().isBadRequest
    }
}
