package com.cvix.authentication.infrastructure.http

import com.cvix.authentication.application.AuthenticateUserQueryHandler
import com.cvix.authentication.application.query.AuthenticateUserQuery
import com.cvix.authentication.domain.AccessToken
import com.cvix.authentication.infrastructure.cookie.AuthCookieBuilder.buildCookies
import com.cvix.authentication.infrastructure.http.request.LoginRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for handling user authentication operations.
 *
 * This controller manages user login functionality, including:
 * - Email and password validation
 * - JWT access token generation
 * - Refresh token generation and storage in HTTP-only cookies
 * - Optional "Remember Me" functionality for extended sessions
 *
 * Uses header-based API versioning (API-Version: v1).
 *
 * ## Security Features:
 * - Password validation against security requirements
 * - Rate limiting to prevent brute force attacks
 * - HTTP-only cookies for refresh tokens (not accessible via JavaScript)
 * - Secure cookie flags in production
 *
 * @property authenticateUserQueryHandler The query handler used for authenticating users.
 * @created 31/7/23
 */
@Validated
@RestController
@RequestMapping("/api", produces = ["application/vnd.api.v1+json"])
@Tag(
    name = "Authentication",
    description = "User authentication and session management endpoints",
)
class UserAuthenticatorController(
    private val authenticateUserQueryHandler: AuthenticateUserQueryHandler,
) {
    /**
     * Authenticates a user with email and password.
     *
     * This endpoint validates user credentials and returns JWT access and refresh tokens.
     * The access token is returned in the response body, while the refresh token is set
     * as an HTTP-only cookie for security.
     *
     * If `rememberMe` is true, the refresh token TTL is extended (e.g., 30 days instead of 7 days).
     *
     * @param loginRequest The login request containing email, password, and rememberMe flag.
     * @param response The HTTP response used to set authentication cookies.
     * @return ResponseEntity with AccessToken containing JWT tokens and user information.
     */
    @Operation(
        summary = "Authenticate user (login)",
        description = "Authenticates a user with email and password, returning JWT access and refresh tokens. " +
            "The access token is returned in the response body, while the refresh token is set as an " +
            "HTTP-only secure cookie. Optionally extends session duration if rememberMe is true.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Authentication successful - Access token returned, refresh token set in cookie",
                content = [Content(schema = Schema(implementation = AccessToken::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data - Email format invalid or password does not meet requirements",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Authentication failed - Invalid credentials (email or password incorrect)",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "429",
                description = "Too many authentication attempts - Rate limit exceeded",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error during authentication",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
        ],
    )
    @PostMapping(LOGIN_ROUTE, consumes = ["application/json"])
    suspend fun login(
        @Validated @RequestBody loginRequest: LoginRequest,
        response: ServerHttpResponse,
    ): ResponseEntity<AccessToken> {
        log.debug("Logging a user in with email: {} (rememberMe: {})", loginRequest.email, loginRequest.rememberMe)

        val (email, password, rememberMe) = loginRequest
        val authenticateUserQuery = AuthenticateUserQuery(
            email = email,
            password = password,
            rememberMe = rememberMe,
        )

        val accessToken = authenticateUserQueryHandler.handle(authenticateUserQuery)

        // Set refresh token as HTTP-only cookie
        buildCookies(response, accessToken, rememberMe)

        log.info("User authenticated successfully: {}", email)

        return ResponseEntity.ok(accessToken)
    }

    companion object {
        const val LOGIN_ROUTE = "/auth/login"
        private val log = org.slf4j.LoggerFactory.getLogger(UserAuthenticatorController::class.java)
    }
}
