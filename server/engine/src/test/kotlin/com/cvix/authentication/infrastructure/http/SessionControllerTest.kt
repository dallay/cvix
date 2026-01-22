package com.cvix.authentication.infrastructure.http

import com.cvix.UnitTest
import com.cvix.authentication.application.query.GetUserSessionQuery
import com.cvix.authentication.domain.UserSession
import com.cvix.authentication.domain.error.InvalidTokenException
import com.cvix.authentication.infrastructure.cookie.AuthCookieBuilder
import com.cvix.common.domain.bus.Mediator
import io.mockk.coEvery
import io.mockk.mockk
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

@UnitTest
internal class SessionControllerTest {

    private lateinit var mediator: Mediator
    private lateinit var sessionController: SessionController
    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp() {
        mediator = mockk()
        sessionController = SessionController(mediator)
        webTestClient = WebTestClient.bindToController(sessionController).build()
    }

    @Test
    @DisplayName("should return session data when access token is valid")
    fun `should return session data when access token is valid`(): Unit = runTest {
        coEvery { mediator.send(GetUserSessionQuery(VALID_ACCESS_TOKEN)) } returns EXPECTED_USER_SESSION

        webTestClient.get().uri("/api/auth/session")
            .cookie(AuthCookieBuilder.ACCESS_TOKEN, VALID_ACCESS_TOKEN)
            .exchange()
            .expectStatus().isOk
            .expectBody(UserSession::class.java)
            .isEqualTo(EXPECTED_USER_SESSION)
    }

    @Test
    @DisplayName("should return 401 when access token is missing")
    fun `should return 401 when access token is missing`(): Unit = runTest {
        webTestClient.get().uri("/api/auth/session")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    @DisplayName("should return 401 when access token is invalid")
    fun `should return 401 when access token is invalid`(): Unit = runTest {
        coEvery {
            mediator.send(GetUserSessionQuery(INVALID_ACCESS_TOKEN))
        } throws InvalidTokenException("Invalid access token")

        webTestClient.get().uri("/api/auth/session")
            .cookie(AuthCookieBuilder.ACCESS_TOKEN, INVALID_ACCESS_TOKEN)
            .exchange()
            .expectStatus().isUnauthorized
    }

    companion object {
        private const val VALID_ACCESS_TOKEN = "valid-access-token"

        @Suppress("MaxLineLength", "MaximumLineLength")
        private const val INVALID_ACCESS_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30"

        private val TEST_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        private const val TEST_EMAIL = "test@example.com"
        private val TEST_ROLES = listOf("USER")

        private val EXPECTED_USER_SESSION = UserSession(
            userId = TEST_USER_ID,
            email = TEST_EMAIL,
            roles = TEST_ROLES,
        )
    }
}
