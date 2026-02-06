package com.cvix.identity.infrastructure.authentication

import com.cvix.UnitTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@UnitTest
internal class AuthenticationExceptionAdviceIT {

    private val webTestClient = WebTestClient.bindToController(AccountExceptionResource())
        .controllerAdvice(GeneralAuthAdvice())
        .build()

    @Test
    fun `should handle NotAuthenticatedUserException`() {
        webTestClient.get().uri("/api/account-exceptions/not-authenticated")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.message").isEqualTo("error.http.401")
            .jsonPath("$.title").isEqualTo("not authenticated")
    }

    @Test
    fun `should handle UnknownAuthenticationException`() {
        webTestClient.get().uri("/api/account-exceptions/unknown-authentication")
            .exchange()
            .expectStatus().is5xxServerError
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.message").isEqualTo("error.http.500")
            .jsonPath("$.title").isEqualTo("unknown authentication")
    }
}
