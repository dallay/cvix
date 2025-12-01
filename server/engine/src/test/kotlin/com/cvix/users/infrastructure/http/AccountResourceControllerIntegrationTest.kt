package com.cvix.users.infrastructure.http

import com.cvix.authentication.domain.AuthoritiesConstants
import com.cvix.config.InfrastructureTestContainers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOAuth2Login
import org.springframework.test.web.reactive.server.WebTestClient

private const val ENDPOINT = "/api/account"

@AutoConfigureWebTestClient
class AccountResourceControllerIntegrationTest : InfrastructureTestContainers() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `should get account information successfully`() {
        webTestClient.mutateWith(
            oAuth2LoginMutator(),
        )
            .get().uri(ENDPOINT)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("c4af4e2f-b432-4c3b-8405-cca86cd5b97b")
            .jsonPath("$.username").isEqualTo("user")
            .jsonPath("$.email").isEqualTo("user@cvix.com")
            .jsonPath("$.firstname").isEqualTo("Basic")
            .jsonPath("$.lastname").isEqualTo("User")
            .jsonPath("$.authorities").isArray.jsonPath("$.authorities[0]")
            .isEqualTo(AuthoritiesConstants.USER)
    }

    @Test
    fun `should get account information successfully with multiple roles`() {
        webTestClient.mutateWith(
            oAuth2LoginMutator(roles = listOf(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN)),
        )
            .get().uri(ENDPOINT)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("c4af4e2f-b432-4c3b-8405-cca86cd5b97b")
            .jsonPath("$.username").isEqualTo("user")
            .jsonPath("$.email").isEqualTo("user@cvix.com")
            .jsonPath("$.firstname").isEqualTo("Basic")
            .jsonPath("$.lastname").isEqualTo("User")
            .jsonPath("$.authorities").isArray.jsonPath("$.authorities[0]")
            .isEqualTo(AuthoritiesConstants.USER)
            .jsonPath("$.authorities[1]")
            .isEqualTo(AuthoritiesConstants.ADMIN)
    }

    private fun oAuth2LoginMutator(
        userId: String = "c4af4e2f-b432-4c3b-8405-cca86cd5b97b",
        username: String = "user",
        email: String = "user@cvix.com",
        firstname: String = "Basic",
        lastname: String = "User",
        roles: List<String> = listOf(AuthoritiesConstants.USER)
    ): SecurityMockServerConfigurers.OAuth2LoginMutator =
        mockOAuth2Login().authorities(SimpleGrantedAuthority(roles.joinToString(separator = ",") { it })).attributes {
            it["sub"] = userId // Keycloak user ID
            it["preferred_username"] = username
            it["email"] = email
            it["given_name"] = firstname
            it["family_name"] = lastname
            it["roles"] = roles
        }
}
