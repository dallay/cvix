package com.loomify.engine.authentication.infrastructure.cookie

import com.loomify.engine.authentication.domain.AccessToken
import org.springframework.http.ResponseCookie
import org.springframework.http.server.reactive.ServerHttpResponse
import java.time.Duration

/**
 * [AuthCookieBuilder] is a utility class that builds cookies for the authentication process.
 * It provides a method to build cookies for the access token.
 */
object AuthCookieBuilder {
    const val ACCESS_TOKEN = "ACCESS_TOKEN"
    const val REFRESH_TOKEN = "REFRESH_TOKEN"
    private const val SESSION = "SESSION"
    private const val SAME_SITE_LAX = "Lax"

    // Token durations
    private val REMEMBER_ME_REFRESH_TOKEN_DURATION = Duration.ofDays(30)  // 30 days for remember me
    private val STANDARD_REFRESH_TOKEN_DURATION = Duration.ofDays(1)      // 1 day for standard login

    /**
     * Builds cookies for the access token.
     * Adds the access token, refresh token, and session state cookies to the response.
     *
     * @param response The ServerHttpResponse to which the cookies are added.
     * @param accessToken The access token containing the token, refresh token, and session state.
     * @param rememberMe Whether to use extended cookie duration for remember-me functionality.
     */
    fun buildCookies(
        response: ServerHttpResponse,
        accessToken: AccessToken,
        rememberMe: Boolean = false
    ) {
        // Access token always uses short duration from the token itself
        response.addCookie(
            ResponseCookie.from(ACCESS_TOKEN, accessToken.token)
                .path("/")
                .maxAge(accessToken.expiresIn)
                .httpOnly(true)
                .secure(true)
                .sameSite(SAME_SITE_LAX)
                .build(),
        )

        // Refresh token duration varies based on rememberMe
        val refreshTokenMaxAge = if (rememberMe) {
            REMEMBER_ME_REFRESH_TOKEN_DURATION.seconds
        } else {
            STANDARD_REFRESH_TOKEN_DURATION.seconds
        }

        response.addCookie(
            ResponseCookie.from(REFRESH_TOKEN, accessToken.refreshToken)
                .path("/")
                .maxAge(refreshTokenMaxAge)
                .httpOnly(true)
                .secure(true)
                .sameSite(SAME_SITE_LAX)
                .build(),
        )

        if (accessToken.sessionState != null) {
            response.addCookie(
                ResponseCookie.from(SESSION, accessToken.sessionState)
                    .path("/")
                    .maxAge(refreshTokenMaxAge)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite(SAME_SITE_LAX)
                    .build(),
            )
        }
    }

    fun clearCookies(response: ServerHttpResponse) {
        response.addCookie(
            ResponseCookie.from(ACCESS_TOKEN, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(true)
                .sameSite(SAME_SITE_LAX)
                .build(),
        )
        response.addCookie(
            ResponseCookie.from(REFRESH_TOKEN, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(true)
                .sameSite(SAME_SITE_LAX)
                .build(),
        )
        response.addCookie(
            ResponseCookie.from(SESSION, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(true)
                .sameSite(SAME_SITE_LAX)
                .build(),
        )
    }
}
