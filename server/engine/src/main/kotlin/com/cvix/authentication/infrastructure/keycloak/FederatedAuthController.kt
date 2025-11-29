package com.cvix.authentication.infrastructure.keycloak

import com.cvix.authentication.application.FederatedAuthService
import java.net.URI
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

private const val GITHUB = "github"

private const val MICROSOFT = "microsoft"

private const val GOOGLE = "google"

/**
 * REST controller for handling federated authentication (OAuth2/OIDC) flows.
 * Provides endpoints for initiating federated login and handling OAuth callbacks.
 *
 * This controller implements the Authorization Code Flow with PKCE for federated
 * identity providers (Google, Microsoft, GitHub) via Keycloak.
 *
 * **Requirements**: FR-024, FR-025, FR-026
 *
 * @property federatedAuthService Service for managing federated identities
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/auth/federated")
class FederatedAuthController(
    private val federatedAuthService: FederatedAuthService
) {
    /**
     * Initiates the federated authentication flow by redirecting to the identity provider.
     * Uses Spring Security's OAuth2 login flow with PKCE.
     *
     * **Requirements**: FR-024, FR-025
     *
     * @param provider Identity provider name (google, microsoft, github)
     * @param redirectUri Optional URI to redirect to after authentication
     * @param exchange The server web exchange
     * @return Mono with redirect response to OAuth provider
     */
    @GetMapping("/initiate")
    fun initiateFederatedAuth(
        @RequestParam provider: String,
        @RequestParam(required = false, defaultValue = "/dashboard") redirectUri: String,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Void>> {
        // Validate provider
        val validProviders = listOf(GOOGLE, MICROSOFT, GITHUB)
        if (provider !in validProviders) {
            return Mono.just(
                ResponseEntity.badRequest().build(),
            )
        }

        // Store redirect URI in session for later use
        return exchange.session.flatMap { session ->
            session.attributes["OAUTH2_REDIRECT_URI"] = redirectUri

            // Redirect to Spring Security OAuth2 authorization endpoint
            val authorizationUrl = "/oauth2/authorization/$provider"

            Mono.just(
                ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(authorizationUrl))
                    .build(),
            )
        }
    }

    /**
     * Handles the OAuth2 callback after successful authentication with the identity provider.
     * Creates or links user account and establishes an authenticated session.
     *
     * **Requirements**: FR-024, FR-025, FR-026
     *
     * @param oidcUser The authenticated OIDC user from the identity provider
     * @param exchange The server web exchange
     * @return Mono with redirect response to the application
     */
    @GetMapping("/callback")
    fun handleOAuthCallback(
        @AuthenticationPrincipal oidcUser: OidcUser,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Void>> {
        return exchange.session.flatMap { session ->
            // Get the stored redirect URI or default to dashboard
            val redirectUri = session.attributes.remove("OAUTH2_REDIRECT_URI") as? String ?: "/dashboard"

            // Extract user information from OIDC token
            val email = oidcUser.email
            val firstName = oidcUser.givenName ?: ""
            val lastName = oidcUser.familyName ?: ""
            val externalUserId = oidcUser.subject

            // Get provider from issuer
            val issuer = oidcUser.issuer?.toString() ?: ""
            val provider = when {
                issuer.contains(GOOGLE) -> GOOGLE
                issuer.contains(MICROSOFT) || issuer.contains("live.com") -> MICROSOFT
                issuer.contains(GITHUB) -> GITHUB
                else -> "unknown"
            }

            // Find or create user account
            federatedAuthService.findOrCreateUser(
                provider = provider,
                externalUserId = externalUserId,
                email = email,
                firstName = firstName,
                lastName = lastName,
                displayName = oidcUser.fullName ?: "$firstName $lastName",
            ).flatMap { _ ->
                // Spring Security OAuth2 login automatically manages the session
                // No need to manually set cookies as the OAuth2LoginAuthenticationWebFilter handles this

                // Redirect to application
                Mono.just(
                    ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(redirectUri))
                        .build(),
                )
            }
        }
    }

    /**
     * Gets the current federated authentication status.
     * Used by the frontend to check if a federated login is in progress.
     *
     * @param oidcUser The authenticated OIDC user (if any)
     * @return Mono with authentication status
     */
    @GetMapping("/status")
    fun getFederatedAuthStatus(
        @AuthenticationPrincipal oidcUser: OidcUser?
    ): Mono<ResponseEntity<Map<String, Any>>> {
        return if (oidcUser != null) {
            Mono.just(
                ResponseEntity.ok(
                    mapOf(
                        "authenticated" to true,
                        "provider" to (oidcUser.issuer?.toString() ?: "unknown"),
                        "email" to oidcUser.email,
                        "displayName" to (oidcUser.fullName ?: "${oidcUser.givenName} ${oidcUser.familyName}"),
                    ),
                ),
            )
        } else {
            Mono.just(
                ResponseEntity.ok(
                    mapOf("authenticated" to false),
                ),
            )
        }
    }
}
