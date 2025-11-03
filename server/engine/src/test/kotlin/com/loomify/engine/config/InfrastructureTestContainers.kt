package com.loomify.engine.config

import com.loomify.IntegrationTest
import com.loomify.engine.authentication.domain.AccessToken
import com.loomify.engine.authentication.infrastructure.mapper.AccessTokenResponseMapper.toAccessToken
import java.net.URI
import java.net.URISyntaxException
import org.keycloak.representations.AccessTokenResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Base class for integration tests requiring external infrastructure (PostgreSQL, Keycloak, GreenMail).
 * Uses singleton Testcontainers managed by TestcontainersManager for optimal performance and reliability.
 * 
 * All containers are started once per test suite and reused across test classes to:
 * - Reduce test execution time (containers start once, not per test class)
 * - Eliminate flakiness from container startup timing issues
 * - Enable container reuse between test runs when configured
 * 
 * @see TestcontainersManager
 */
@Testcontainers
@IntegrationTest
abstract class InfrastructureTestContainers {
    @Value("\${testing.security.username}")
    protected lateinit var testUsername: String

    @Value("\${testing.security.password}")
    protected lateinit var testPassword: String

    /**
     * Obtains an access token from Keycloak for testing.
     * Uses the singleton Keycloak container from TestcontainersManager.
     */
    protected fun getAccessToken(
        username: String = testUsername,
        password: String = testPassword
    ): AccessToken? =
        try {
            val authServerUrl = removeLastSlash(TestcontainersManager.keycloakContainer.authServerUrl)
            val openIdConnectToken = "protocol/openid-connect/token"
            val realm = "loomify"
            val clientId = "web_app"
            val authorizationURI = URI("$authServerUrl/realms/$realm/$openIdConnectToken")
            val webclient = WebClient.builder().build()
            val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
            formData.add("grant_type", "password")
            formData.add("client_id", clientId)
            formData.add("username", username)
            formData.add("password", password)

            val result = webclient.post()
                .uri(authorizationURI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(AccessTokenResponse::class.java)
                .block()

            result?.toAccessToken()
        } catch (e: URISyntaxException) {
            log.error("Can't obtain an access token from Keycloak!", e)
            null
        }

    companion object {
        private val log = LoggerFactory.getLogger(InfrastructureTestContainers::class.java)

        /**
         * Registers dynamic properties for Spring from Testcontainers.
         * This method is called once by Spring Test Framework before any tests run.
         */
        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            log.info("Registering dynamic properties from Testcontainers...")
            
            // Ensure all containers are started before registering properties
            TestcontainersManager.startAll()
            
            // Register Keycloak properties
            TestcontainersManager.getKeycloakProperties().forEach { (key, value) ->
                registry.add(key) { value }
            }
            
            // Register Mail properties
            TestcontainersManager.getMailProperties().forEach { (key, value) ->
                registry.add(key) { value }
            }
            
            // PostgreSQL is automatically configured via @ServiceConnection in TestcontainersConfiguration
            // No need to manually register R2DBC properties here
            
            log.info("Dynamic properties registered successfully")
        }

        private fun removeLastSlash(url: String): String {
            if (url.length > 1 && url.endsWith("/")) {
                return url.substring(0, url.length - 1)
            }
            return url
        }
    }
}
