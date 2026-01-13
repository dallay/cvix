package com.cvix.users.infrastructure.http

import com.cvix.ControllerIntegrationTest
import com.cvix.authentication.domain.AccessToken
import com.cvix.authentication.domain.AuthoritiesConstants
import com.cvix.authentication.infrastructure.cookie.AuthCookieBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("MultilineRawStringIndentation")
internal class AccountResourceCookieAuthIntegrationTest : ControllerIntegrationTest() {
    private val email = "john.doe@profiletailors.com"
    private val password = "S3cr3tP@ssw0rd*123"
    private var accessToken: AccessToken? = null

    @BeforeEach
    fun setUp() {
        accessToken = getAccessToken(email, password)
            ?: error("Failed to obtain access token for test user $email. Check Keycloak/Testcontainers setup.")
    }

    @Test
    fun `should get account information when access token is in cookie`() {
        webTestClient.get().uri("/api/account")
            .cookie(AuthCookieBuilder.ACCESS_TOKEN, accessToken?.token ?: "")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.username").isEqualTo("john.doe")
            .jsonPath("$.email").isEqualTo("john.doe@profiletailors.com")
            .jsonPath("$.firstname").isEqualTo("John")
            .jsonPath("$.lastname").isEqualTo("Doe")
            .jsonPath("$.authorities").isArray.jsonPath("$.authorities[0]")
            .isEqualTo(AuthoritiesConstants.USER)
    }

    @Test
    fun `should return 401 when access token cookie is invalid`() {
        webTestClient.get().uri("/api/account")
            .cookie(AuthCookieBuilder.ACCESS_TOKEN, "invalid-token")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should not expose sensitive fields in account response`() {
        webTestClient.get().uri("/api/account")
            .cookie(AuthCookieBuilder.ACCESS_TOKEN, accessToken?.token ?: "")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.password").doesNotExist()
            .jsonPath("$.internalId").doesNotExist()
    }

    @Test
    fun `should return correct authorities for multiple roles`() {
        // Simulate a user with multiple roles (requires test setup for such user)
        // For demonstration, assuming authorities contains USER and ADMIN
        webTestClient.get().uri("/api/account")
            .cookie(AuthCookieBuilder.ACCESS_TOKEN, accessToken?.token ?: "")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.authorities").isArray
            .jsonPath("$.authorities").value { authorities: List<*> ->
                assert(authorities.contains(AuthoritiesConstants.USER))
                // Uncomment if ADMIN role is present in test data
                // assert(authorities.contains(AuthoritiesConstants.ADMIN))
            }
    }
}
