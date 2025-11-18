package com.loomify.engine.authentication.infrastructure.persistence.keycloak

import com.loomify.IntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient

@IntegrationTest
@AutoConfigureWebTestClient
class KeycloakLogoutRepositoryIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `should logout from keycloak`() {
        // This is a placeholder for the actual test logic.
        // I will need to implement the actual test logic in the next step.
    }
}
