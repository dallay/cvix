package com.cvix.identity.infrastructure.authentication.http

import com.cvix.ControllerIntegrationTest
import com.cvix.common.domain.authentication.AccessToken
import com.cvix.identity.domain.authentication.UserSession
import com.cvix.identity.infrastructure.authentication.cookie.AuthCookieBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf

private const val ENDPOINT = "/api/auth/session"

internal class SessionControllerIntegrationTest : ControllerIntegrationTest() {

    private var accessToken: AccessToken? = null

    @BeforeEach
    fun setUp() {
        accessToken = getAccessToken()
    }

    @Test
    @DisplayName("should return user session when access token is valid")
    fun `should return user session when access token is valid`() {
        webTestClient
            .mutateWith(csrf())
            .get()
            .uri(ENDPOINT)
            .cookie(AuthCookieBuilder.ACCESS_TOKEN, accessToken?.token ?: "")
            .cookie(AuthCookieBuilder.REFRESH_TOKEN, accessToken?.refreshToken ?: "")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType("application/vnd.api.v1+json")
            .expectBody(UserSession::class.java)
            .consumeWith { result ->
                val userSession = result.responseBody!!
                assertEquals(testUsername, userSession.email)
                assertTrue(userSession.roles.isNotEmpty())
                assertTrue(userSession.roles.contains("ROLE_USER"))
            }
    }

    @Test
    @DisplayName("should return user session with correct JSON structure")
    fun `should return user session with correct JSON structure`() {
        webTestClient
            .mutateWith(csrf())
            .get()
            .uri(ENDPOINT)
            .cookie(AuthCookieBuilder.ACCESS_TOKEN, accessToken?.token ?: "")
            .cookie(AuthCookieBuilder.REFRESH_TOKEN, accessToken?.refreshToken ?: "")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType("application/vnd.api.v1+json")
            .expectBody()
            .jsonPath("$.userId").isNotEmpty
            .jsonPath("$.email").isEqualTo(testUsername)
            .jsonPath("$.roles").isArray
            .jsonPath("$.roles").isNotEmpty
    }

    @Test
    @DisplayName("should return 401 when access token cookie is missing")
    fun `should return 401 when access token cookie is missing`() {
        webTestClient
            .mutateWith(csrf())
            .get()
            .uri(ENDPOINT)
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody().isEmpty
    }

    @Test
    @DisplayName("should return 401 when access token is invalid")
    fun `should return 401 when access token is invalid`() {
        val invalidToken = "invalid.jwt.token"

        webTestClient
            .mutateWith(csrf())
            .get()
            .uri(ENDPOINT)
            .cookie(AuthCookieBuilder.ACCESS_TOKEN, invalidToken)
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody().isEmpty
    }

    @Test
    @DisplayName("should return 401 when access token is expired")
    @Suppress("MaxLineLength", "MaximumLineLength")
    fun `should return 401 when access token is expired`() {
        val expiredToken =
            "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJyUEk2Qkl5Y2lpMV9EUFVocGdSTGFnQWF1RlhXTkhFZmZQbDdFZV9DUVhFIn0.eyJleHAiOjE2MDk0NTk4MDAsImlhdCI6MTYwOTQ1OTIwMCwianRpIjoiY2E5NzM3ZTUtNzY5My00Mzk4LWFiYjItOGUzOTNlNzJlMTY1IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL2F1dGgvcmVhbG1zL2hhdGNoZ3JpZCIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIxMjM0NTY3OC0xMjM0LTEyMzQtMTIzNC0xMjM0NTY3ODkwMTIiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJ3ZWJfYXBwIiwic2Vzc2lvbl9zdGF0ZSI6IjEyMzQ1Njc4LTEyMzQtMTIzNC0xMjM0LTEyMzQ1Njc4OTAxMiIsImVtYWlsIjoidGVzdEBleGFtcGxlLmNvbSJ9.invalid-signature"

        webTestClient
            .mutateWith(csrf())
            .get()
            .uri(ENDPOINT)
            .cookie(AuthCookieBuilder.ACCESS_TOKEN, expiredToken)
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody().isEmpty
    }

    @Test
    @DisplayName("should return 401 when access token has invalid format")
    fun `should return 401 when access token has invalid format`() {
        val malformedToken = "not.a.valid.jwt.format.at.all"

        webTestClient
            .mutateWith(csrf())
            .get()
            .uri(ENDPOINT)
            .cookie(AuthCookieBuilder.ACCESS_TOKEN, malformedToken)
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody().isEmpty
    }
}
