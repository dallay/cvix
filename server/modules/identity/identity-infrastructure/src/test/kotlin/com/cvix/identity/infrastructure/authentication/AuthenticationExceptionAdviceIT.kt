package com.cvix.identity.infrastructure.authentication

import com.cvix.identity.infrastructure.TestIdentityApplication
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [TestIdentityApplication::class],
)
@AutoConfigureWebTestClient
@TestPropertySource(properties = ["server.ssl.enabled=false"])
class AuthenticationExceptionAdviceIT {

    @Autowired
    private lateinit var webTestClient: WebTestClient

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
