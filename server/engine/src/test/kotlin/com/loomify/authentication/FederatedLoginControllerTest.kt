package com.loomify.authentication

import com.loomify.authentication.application.FederatedAuthService
import com.loomify.authentication.application.UserInfo
import com.loomify.authentication.domain.Role
import com.loomify.authentication.infrastructure.keycloak.FederatedAuthController
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.URI
import java.time.Instant
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@DisplayName("Federated Login Controller Tests")
class FederatedLoginControllerTest {
    private lateinit var federatedAuthService: FederatedAuthService
    private lateinit var controller: FederatedAuthController
    private lateinit var exchange: ServerWebExchange
    private lateinit var session: WebSession
    private lateinit var response: ServerHttpResponse

    @BeforeEach
    fun setup() {
        federatedAuthService = mockk()
        controller = FederatedAuthController(federatedAuthService)
        exchange = mockk(relaxed = true)
        session = mockk(relaxed = true)
        response = mockk(relaxed = true)
    }

    @Nested
    @DisplayName("Initiate Federated Auth")
    inner class InitiateFederatedAuth {
        @Test
        fun `should initiate Google OAuth flow`() {
            // Given
            val provider = "google"
            val redirectUri = "/dashboard"
            val sessionAttributes = mutableMapOf<String, Any>()

            every { exchange.session } returns Mono.just(session)
            every { session.attributes } returns sessionAttributes

            // When
            val result = controller.initiateFederatedAuth(provider, redirectUri, exchange)

            // Then
            StepVerifier.create(result)
                .expectNextMatches { response ->
                    response.statusCode == HttpStatus.FOUND &&
                        response.headers.location == URI.create("/oauth2/authorization/google") &&
                        sessionAttributes["OAUTH2_REDIRECT_URI"] == redirectUri
                }
                .verifyComplete()
        }

        @Test
        fun `should initiate Microsoft OAuth flow`() {
            // Given
            val provider = "microsoft"
            val redirectUri = "/workspace/abc123"
            val sessionAttributes = mutableMapOf<String, Any>()

            every { exchange.session } returns Mono.just(session)
            every { session.attributes } returns sessionAttributes

            // When
            val result = controller.initiateFederatedAuth(provider, redirectUri, exchange)

            // Then
            StepVerifier.create(result)
                .expectNextMatches { response ->
                    response.statusCode == HttpStatus.FOUND &&
                        response.headers.location == URI.create("/oauth2/authorization/microsoft") &&
                        sessionAttributes["OAUTH2_REDIRECT_URI"] == redirectUri
                }
                .verifyComplete()
        }

        @Test
        fun `should initiate GitHub OAuth flow`() {
            // Given
            val provider = "github"
            val redirectUri = "/profile"
            val sessionAttributes = mutableMapOf<String, Any>()

            every { exchange.session } returns Mono.just(session)
            every { session.attributes } returns sessionAttributes

            // When
            val result = controller.initiateFederatedAuth(provider, redirectUri, exchange)

            // Then
            StepVerifier.create(result)
                .expectNextMatches { response ->
                    response.statusCode == HttpStatus.FOUND &&
                        response.headers.location == URI.create("/oauth2/authorization/github") &&
                        sessionAttributes["OAUTH2_REDIRECT_URI"] == redirectUri
                }
                .verifyComplete()
        }

        @Test
        fun `should reject invalid provider`() {
            // Given
            val provider = "invalid"
            val redirectUri = "/dashboard"

            every { exchange.session } returns Mono.just(session)

            // When
            val result = controller.initiateFederatedAuth(provider, redirectUri, exchange)

            // Then
            StepVerifier.create(result)
                .expectNextMatches { response ->
                    response.statusCode == HttpStatus.BAD_REQUEST
                }
                .verifyComplete()
        }

        @Test
        fun `should use default redirect URI when not provided`() {
            // Given
            val provider = "google"
            val sessionAttributes = mutableMapOf<String, Any>()

            every { exchange.session } returns Mono.just(session)
            every { session.attributes } returns sessionAttributes

            // When
            val result = controller.initiateFederatedAuth(provider, "/dashboard", exchange)

            // Then
            StepVerifier.create(result)
                .expectNextMatches { response ->
                    sessionAttributes["OAUTH2_REDIRECT_URI"] == "/dashboard"
                }
                .verifyComplete()
        }
    }

    @Nested
    @DisplayName("Handle OAuth Callback")
    inner class HandleOAuthCallback {
        private fun createMockOidcUser(
            email: String,
            givenName: String,
            familyName: String,
            subject: String,
            issuer: String
        ): OidcUser {
            val idToken = OidcIdToken.withTokenValue("mock-token")
                .subject(subject)
                .claim("email", email)
                .claim("given_name", givenName)
                .claim("family_name", familyName)
                .claim("name", "$givenName $familyName")
                .issuer(issuer)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build()

            return DefaultOidcUser(emptyList(), idToken)
        }

        @Test
        fun `should handle Google OAuth callback and create new user`() {
            // Given
            val oidcUser = createMockOidcUser(
                email = "jane.doe@gmail.com",
                givenName = "Jane",
                familyName = "Doe",
                subject = "google-user-123",
                issuer = "https://accounts.google.com",
            )

            val sessionAttributes = mutableMapOf<String, Any>("OAUTH2_REDIRECT_URI" to "/dashboard")
            val authenticatedUser = UserInfo(
                id = UUID.randomUUID(),
                email = "jane.doe@gmail.com",
                username = "jane.doe@gmail.com",
                firstName = "Jane",
                lastName = "Doe",
                displayName = "Jane Doe",
                roles = setOf(Role.USER),
            )

            every { exchange.session } returns Mono.just(session)
            every { session.attributes } returns sessionAttributes
            every { exchange.response } returns response
            every {
                federatedAuthService.findOrCreateUser(
                    provider = "google",
                    externalUserId = "google-user-123",
                    email = "jane.doe@gmail.com",
                    firstName = "Jane",
                    lastName = "Doe",
                    displayName = "Jane Doe",
                )
            } returns Mono.just(authenticatedUser)

            // When
            val result = controller.handleOAuthCallback(oidcUser, exchange)

            // Then
            StepVerifier.create(result)
                .expectNextMatches { response ->
                    response.statusCode == HttpStatus.FOUND &&
                        response.headers.location == URI.create("/dashboard")
                }
                .verifyComplete()

            // Spring Security OAuth2 login automatically manages cookies, so no need to verify
        }

        @Test
        fun `should handle Microsoft OAuth callback`() {
            // Given
            val oidcUser = createMockOidcUser(
                email = "john.smith@outlook.com",
                givenName = "John",
                familyName = "Smith",
                subject = "microsoft-user-456",
                issuer = "https://login.microsoftonline.com/common/v2.0",
            )

            val sessionAttributes = mutableMapOf<String, Any>("OAUTH2_REDIRECT_URI" to "/workspace/xyz")
            val authenticatedUser = UserInfo(
                id = UUID.randomUUID(),
                email = "john.smith@outlook.com",
                username = "john.smith@outlook.com",
                firstName = "John",
                lastName = "Smith",
                displayName = "John Smith",
                roles = setOf(Role.USER),
            )

            every { exchange.session } returns Mono.just(session)
            every { session.attributes } returns sessionAttributes
            every { exchange.response } returns response
            every {
                federatedAuthService.findOrCreateUser(
                    provider = "microsoft",
                    externalUserId = "microsoft-user-456",
                    email = "john.smith@outlook.com",
                    firstName = "John",
                    lastName = "Smith",
                    displayName = "John Smith",
                )
            } returns Mono.just(authenticatedUser)

            // When
            val result = controller.handleOAuthCallback(oidcUser, exchange)

            // Then
            StepVerifier.create(result)
                .expectNextMatches { response ->
                    response.statusCode == HttpStatus.FOUND &&
                        response.headers.location == URI.create("/workspace/xyz")
                }
                .verifyComplete()
        }

        @Test
        fun `should handle GitHub OAuth callback`() {
            // Given
            val oidcUser = createMockOidcUser(
                email = "developer@github.com",
                givenName = "Dev",
                familyName = "User",
                subject = "github-user-789",
                issuer = "https://github.com",
            )

            val sessionAttributes = mutableMapOf<String, Any>()
            val authenticatedUser = UserInfo(
                id = UUID.randomUUID(),
                email = "developer@github.com",
                username = "developer@github.com",
                firstName = "Dev",
                lastName = "User",
                displayName = "Dev User",
                roles = setOf(Role.USER),
            )

            every { exchange.session } returns Mono.just(session)
            every { session.attributes } returns sessionAttributes
            every { exchange.response } returns response
            every {
                federatedAuthService.findOrCreateUser(
                    provider = "github",
                    externalUserId = "github-user-789",
                    email = "developer@github.com",
                    firstName = "Dev",
                    lastName = "User",
                    displayName = "Dev User",
                )
            } returns Mono.just(authenticatedUser)

            // When
            val result = controller.handleOAuthCallback(oidcUser, exchange)

            // Then
            StepVerifier.create(result)
                .expectNextMatches { response ->
                    response.statusCode == HttpStatus.FOUND &&
                        response.headers.location == URI.create("/dashboard") // default redirect
                }
                .verifyComplete()
        }

        @Test
        fun `should link existing user when email already exists`() {
            // Given
            val existingUserId = UUID.randomUUID()
            val oidcUser = createMockOidcUser(
                email = "existing@example.com",
                givenName = "Existing",
                familyName = "User",
                subject = "google-user-999",
                issuer = "https://accounts.google.com",
            )

            val sessionAttributes = mutableMapOf<String, Any>()
            val authenticatedUser = UserInfo(
                id = existingUserId,
                email = "existing@example.com",
                username = "existing@example.com",
                firstName = "Existing",
                lastName = "User",
                displayName = "Existing User",
                roles = setOf(Role.USER),
            )

            every { exchange.session } returns Mono.just(session)
            every { session.attributes } returns sessionAttributes
            every { exchange.response } returns response
            every {
                federatedAuthService.findOrCreateUser(
                    provider = "google",
                    externalUserId = "google-user-999",
                    email = "existing@example.com",
                    firstName = "Existing",
                    lastName = "User",
                    displayName = "Existing User",
                )
            } returns Mono.just(authenticatedUser)

            // When
            val result = controller.handleOAuthCallback(oidcUser, exchange)

            // Then
            StepVerifier.create(result)
                .expectNextMatches { response ->
                    response.statusCode == HttpStatus.FOUND
                }
                .verifyComplete()

            verify {
                federatedAuthService.findOrCreateUser(any(), any(), any(), any(), any(), any())
            }
        }
    }

    @Nested
    @DisplayName("Get Federated Auth Status")
    inner class GetFederatedAuthStatus {
        @Test
        fun `should return authenticated status when user is authenticated`() {
            // Given
            val oidcUser = createMockOidcUser(
                email = "test@example.com",
                givenName = "Test",
                familyName = "User",
                subject = "test-123",
                issuer = "https://accounts.google.com",
            )

            // When
            val result = controller.getFederatedAuthStatus(oidcUser)

            // Then
            StepVerifier.create(result)
                .expectNextMatches { response ->
                    response.statusCode == HttpStatus.OK &&
                        response.body?.get("authenticated") == true &&
                        response.body?.get("email") == "test@example.com"
                }
                .verifyComplete()
        }

        @Test
        fun `should return not authenticated when user is null`() {
            // When
            val result = controller.getFederatedAuthStatus(null)

            // Then
            StepVerifier.create(result)
                .expectNextMatches { response ->
                    response.statusCode == HttpStatus.OK &&
                        response.body?.get("authenticated") == false
                }
                .verifyComplete()
        }

        private fun createMockOidcUser(
            email: String,
            givenName: String,
            familyName: String,
            subject: String,
            issuer: String
        ): OidcUser {
            val idToken = OidcIdToken.withTokenValue("mock-token")
                .subject(subject)
                .claim("email", email)
                .claim("given_name", givenName)
                .claim("family_name", familyName)
                .claim("name", "$givenName $familyName")
                .issuer(issuer)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build()

            return DefaultOidcUser(emptyList(), idToken)
        }
    }
}
