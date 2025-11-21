package com.loomify.engine.users.infrastructure.http

import com.loomify.IntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient

@IntegrationTest
@AutoConfigureWebTestClient
class AccountResourceCookieAuthIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `should get account with cookie auth`() {
        // This is a placeholder for the actual test logic.
        // I will need to implement the actual test logic in the next step.
    }
}
