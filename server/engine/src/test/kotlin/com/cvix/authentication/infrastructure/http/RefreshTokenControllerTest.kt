package com.cvix.authentication.infrastructure.http

import com.cvix.UnitTest
import com.cvix.authentication.application.RefreshTokenQueryHandler
import com.cvix.authentication.domain.RefreshToken
import com.cvix.authentication.domain.RefreshTokenManager
import com.cvix.authentication.domain.UserRefreshTokenException
import com.cvix.authentication.infrastructure.CookieAdvice
import com.cvix.authentication.infrastructure.UserAuthAdvice
import com.cvix.authentication.infrastructure.cookie.AuthCookieBuilder
import com.cvix.common.domain.authentication.AccessToken
import com.cvix.controllers.GlobalExceptionHandler
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient

private const val ENDPOINT = "/api/auth/refresh-token"

@UnitTest
internal class RefreshTokenControllerTest {

    private val accessToken = createAccessToken()
    private val refreshTokenManager = mockk<RefreshTokenManager>()
    private val messageSource = mockk<MessageSource>(relaxed = true)
    private val refreshTokenQueryHandler = RefreshTokenQueryHandler(refreshTokenManager)
    private val refreshTokenController = RefreshTokenController(refreshTokenQueryHandler)
    private val webTestClient = WebTestClient.bindToController(refreshTokenController)
        .controllerAdvice(
            // Attach authentication-specific advices first so they take precedence over the global handler in tests
            UserAuthAdvice(messageSource),
            CookieAdvice(messageSource),
            GlobalExceptionHandler(messageSource), // keep global as a fallback
        )
        .build()

    @Test
    fun `refreshTokens should return 200 OK with valid refresh token`(): Unit = runTest {
        coEvery { refreshTokenManager.refresh(any(RefreshToken::class)) } returns accessToken

        webTestClient.post()
            .uri(ENDPOINT)
            .cookie(AuthCookieBuilder.REFRESH_TOKEN, "validRefreshToken")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.token").isEqualTo(accessToken.token)
            .jsonPath("$.expiresIn").isEqualTo(accessToken.expiresIn)
            .jsonPath("$.refreshToken").isEqualTo(accessToken.refreshToken)
            .jsonPath("$.refreshExpiresIn").isEqualTo(accessToken.refreshExpiresIn)
            .jsonPath("$.tokenType").isEqualTo(accessToken.tokenType)
            .jsonPath("$.notBeforePolicy").isEqualTo(accessToken.notBeforePolicy ?: 0)
            .jsonPath("$.sessionState").isEqualTo(accessToken.sessionState ?: "")
            .jsonPath("$.scope").isEqualTo(accessToken.scope ?: "")
    }

    @Test
    fun `refreshTokens should return 400 Bad Request when refresh token is missing`(): Unit =
        runTest {
            webTestClient.post()
                .uri(ENDPOINT)
                .exchange()
                .expectStatus().isBadRequest
        }

    @Test
    fun `refreshTokens should return 401 Unauthorized when handler throws UserRefreshTokenException`(): Unit =
        runTest {
            coEvery { refreshTokenManager.refresh(any(RefreshToken::class)) } throws UserRefreshTokenException(
                "Could not refresh access token",
            )

            webTestClient.post()
                .uri(ENDPOINT)
                .cookie(AuthCookieBuilder.REFRESH_TOKEN, "invalidRefreshToken")
                .exchange()
                .expectStatus().isUnauthorized
        }

    @Test
    fun `refreshTokens should return 500 Internal Server Error on unexpected errors`(): Unit =
        runTest {
            coEvery { refreshTokenManager.refresh(any(RefreshToken::class)) } throws RuntimeException(
                "Unexpected error",
            )

            webTestClient.post()
                .uri(ENDPOINT)
                .cookie(AuthCookieBuilder.REFRESH_TOKEN, "validRefreshToken")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }

    private fun createAccessToken(): AccessToken = AccessToken(
        token = "token",
        expiresIn = 1L,
        refreshToken = "refreshToken",
        refreshExpiresIn = 1L,
        tokenType = "tokenType",
        notBeforePolicy = 1,
        sessionState = "sessionState",
        scope = "scope",
    )
}
