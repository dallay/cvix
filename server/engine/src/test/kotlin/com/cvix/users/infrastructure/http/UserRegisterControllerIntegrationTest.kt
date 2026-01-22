package com.cvix.users.infrastructure.http

import com.cvix.common.domain.vo.credential.Credential
import com.cvix.config.InfrastructureTestContainers
import io.kotest.assertions.print.print
import java.util.concurrent.atomic.AtomicInteger
import net.datafaker.Faker
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

private const val ENDPOINT = "/api/auth/register"

@Suppress("MultilineRawStringIndentation")
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
internal class UserRegisterControllerIntegrationTest : InfrastructureTestContainers() {
    private val faker = Faker()

    // Counter to generate unique IPs for each test to avoid rate limiting conflicts
    private val ipCounter = AtomicInteger(0)

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    private val webTestClient: WebTestClient by lazy {
        WebTestClient.bindToApplicationContext(applicationContext)
            .apply(springSecurity())
            .configureClient()
            .build()
    }

    /**
     * Generates a unique IP for each test to ensure rate limiting doesn't interfere between tests.
     */
    private fun uniqueIp(): String = "192.168.1.${ipCounter.incrementAndGet()}"

    @Test
    fun `should not register a new user without csrf token`() {
        webTestClient.post()
            .uri(ENDPOINT)
            .header("X-Forwarded-For", uniqueIp())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "email": "${faker.internet().emailAddress()}",
                    "password": "${Credential.generateRandomCredentialPassword()}",
                    "firstname": "${faker.name().firstName()}",
                    "lastname": "${faker.name().lastName()}"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should register a new user`() {
        webTestClient
            .mutateWith(csrf())
            .post()
            .uri(ENDPOINT)
            .header("X-Forwarded-For", uniqueIp())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "email": "${faker.internet().emailAddress()}",
                    "password": "${Credential.generateRandomCredentialPassword()}",
                    "firstname": "${faker.name().firstName()}",
                    "lastname": "${faker.name().lastName()}"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .consumeWith {
                println(it.responseBody?.print())
            }
    }

    @Test
    fun `should not register a new user with an existing email`() {
        val email = faker.internet().emailAddress()
        val password = Credential.generateRandomCredentialPassword()
        val firstname = faker.name().firstName()
        val lastname = faker.name().lastName()
        val testIp = uniqueIp()

        webTestClient
            .mutateWith(csrf())
            .post()
            .uri(ENDPOINT)
            .header("X-Forwarded-For", testIp)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "email": "$email",
                    "password": "$password",
                    "firstname": "$firstname",
                    "lastname": "$lastname"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .consumeWith {
                println(it.responseBody?.print())
            }

        webTestClient
            .mutateWith(csrf())
            .post()
            .uri(ENDPOINT)
            .header("X-Forwarded-For", testIp)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "email": "$email",
                    "password": "${Credential.generateRandomCredentialPassword()}",
                    "firstname": "${faker.name().firstName()}",
                    "lastname": "${faker.name().lastName()}"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .consumeWith {
                println(it.responseBody?.print())
            }
    }

    @Test
    fun `should not register a new user with an invalid email`() {
        webTestClient
            .mutateWith(csrf())
            .post()
            .uri(ENDPOINT)
            .header("X-Forwarded-For", uniqueIp())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "email": "invalid-email",
                    "password": "${Credential.generateRandomCredentialPassword()}",
                    "firstname": "${faker.name().firstName()}",
                    "lastname": "${faker.name().lastName()}"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .consumeWith {
                println(it.responseBody?.print())
            }
    }

    @Test
    fun `should not register a new user with an invalid password`() {
        val invalidPasswords =
            listOf("invalid-password", "1234", "12345678", "ashihweitwjjw", "WEAKPASSWORD1")
        invalidPasswords.forEach {
            webTestClient
                .mutateWith(csrf())
                .post()
                .uri(ENDPOINT)
                .header("X-Forwarded-For", uniqueIp())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                    """
                    {
                        "email": "${faker.internet().emailAddress()}",
                        "password": "$it",
                        "firstname": "${faker.name().firstName()}",
                        "lastname": "${faker.name().lastName()}"
                    }
                    """.trimIndent(),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .consumeWith {
                    println(it.responseBody?.print())
                }
        }
    }

    @Test
    fun `should not register a new user with an invalid firstname`() {
        webTestClient
            .mutateWith(csrf())
            .post()
            .uri(ENDPOINT)
            .header("X-Forwarded-For", uniqueIp())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "email": "${faker.internet().emailAddress()}",
                    "password": "${Credential.generateRandomCredentialPassword()}",
                    "firstname": "",
                    "lastname": "${faker.name().lastName()}"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .consumeWith {
                println(it.responseBody?.print())
            }
    }

    @Test
    fun `should not register a new user with an invalid lastname`() {
        webTestClient
            .mutateWith(csrf())
            .post()
            .uri(ENDPOINT)
            .header("X-Forwarded-For", uniqueIp())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "email": "${faker.internet().emailAddress()}",
                    "password": "${Credential.generateRandomCredentialPassword()}",
                    "firstname": "${faker.name().firstName()}",
                    "lastname": ""
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .consumeWith {
                println(it.responseBody?.print())
            }
    }
}
