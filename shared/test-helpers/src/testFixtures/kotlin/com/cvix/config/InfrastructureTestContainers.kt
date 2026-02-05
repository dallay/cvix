package com.cvix.config

import com.cvix.IntegrationTest
import com.cvix.common.domain.authentication.AccessToken
import com.cvix.common.util.SystemEnvironment.getEnvOrDefault
import dasniko.testcontainers.keycloak.KeycloakContainer
import java.net.URI
import java.net.URISyntaxException
import java.sql.DriverManager
import java.sql.SQLException
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.jupiter.api.BeforeAll
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
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

private const val WEB_PORT = 6080

private const val DB_PORT = 5432

@IntegrationTest
abstract class InfrastructureTestContainers {
    @Value("\${testing.security.username}")
    protected lateinit var testUsername: String

    @Value("\${testing.security.password}")
    protected lateinit var testPassword: String

    init {
        log.info("Starting infrastructure... \uD83D\uDE80")
        // Ensure containers are started in a thread-safe, idempotent way
        ensureStarted()
    }

    protected fun getAccessToken(
        username: String = testUsername,
        password: String = testPassword
    ): AccessToken? =
        try {
            val authServerUrl = removeLastSlash(keycloakContainer.authServerUrl)
            val openIdConnectToken = "protocol/openid-connect/token"
            val authorizationURI = URI("$authServerUrl/realms/$REALM/$openIdConnectToken")
            val webclient = WebClient.builder().build()
            val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
            formData.add("grant_type", "password")
            formData.add("client_id", CLIENT_ID)
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
         * Unique suffix for test container names. Can be overridden via system property
         * `tc.name.suffix` to support container reuse across test runs.
         */
        private val uniqueTestSuffix: String by lazy {
            System.getProperty("tc.name.suffix") ?: UUID.randomUUID().toString()
        }
        private const val ADMIN_USER: String = "admin"
        private const val ADMIN_PASSWORD: String = "secret"
        private const val REALM: String = "cvix"
        private const val CLIENT_ID: String = "web_app"
        private const val ADMIN_CLI = "admin-cli"
        private const val ADMIN_REALM = "master"
        private val ports = arrayOf(3025, 3110, 3143, 3465, 3993, 3995, WEB_PORT)
        private val NETWORK: Network = Network.newNetwork()

        // Ensure containers are started once in a thread-safe manner
        private val started = AtomicBoolean(false)

        // Named constants to avoid magic numbers in the wait logic
        private const val DEFAULT_JDBC_TIMEOUT_SECONDS: Long = 60
        private const val SLEEP_INTERVAL_MILLIS: Long = 1000
        private const val MILLIS_PER_SECOND: Long = 1000

        @JvmStatic
        private val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer(
            DockerImageName.parse(
                "postgres:${
                    getEnvOrDefault(
                        "POSTGRES_VERSION",
                        "15-alpine",
                    )
                }",
            ),
        )
            .withDatabaseName("cvix_test")
            .withUsername("test")
            .withPassword("test")
            .withCreateContainerCmdModifier { cmd -> cmd.withName("postgres-tests-$uniqueTestSuffix") }
            .withNetwork(NETWORK)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(120)))

        @JvmStatic
        private val keycloakContainer: KeycloakContainer =
            KeycloakContainer("keycloak/keycloak:${getEnvOrDefault("KEYCLOAK_VERSION", "26.2.3")}")
                .withRealmImportFile("keycloak/demo-realm-test.json")
                .withAdminUsername(ADMIN_USER)
                .withAdminPassword(ADMIN_PASSWORD)
                .withCreateContainerCmdModifier { cmd ->
                    cmd.withName("keycloak-tests-$uniqueTestSuffix")
                }
                .withNetwork(NETWORK)
                // Wait for the specific realm to be available (avoid 404 during realm import)
                // Increased timeout to 300s for CI environments with limited resources
                .waitingFor(
                    Wait.forHttp("/realms/$REALM").forStatusCode(200).withStartupTimeout(
                        Duration.ofSeconds(300),
                    ),
                )

        @JvmStatic
        private val greenMailContainer: GenericContainer<*> = GenericContainer<Nothing>(
            DockerImageName.parse(
                "greenmail/standalone:${
                    getEnvOrDefault(
                        "GREENMAIL_VERSION",
                        "2.1.8",
                    )
                }",
            ),
        ).apply {
            withEnv(
                "GREENMAIL_OPTS",
                "-Dgreenmail.setup.test.smtp -Dgreenmail.hostname=0.0.0.0",
            )
            waitingFor(Wait.forLogMessage(".*Starting GreenMail standalone.*", 1))
            // avoid spread operator copy (Detekt complains about spread operator performance)
            // call withExposedPorts for each port to avoid creating a copied array via the spread operator
            ports.forEach { port -> withExposedPorts(port) }
            withCreateContainerCmdModifier { cmd ->
                cmd.withName("greenmail-tests-$uniqueTestSuffix")
            }
            withNetwork(NETWORK)
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerResourceServerIssuerProperty(registry: DynamicPropertyRegistry) {
            // Ensure database props are registered first so Liquibase/R2DBC can bootstrap
            registerDatabaseProperties(registry)
            registerKeycloakProperties(registry)
            registerMailProperties(registry)
        }

        private fun registerDatabaseProperties(registry: DynamicPropertyRegistry) {
            log.info("Registering Database Properties")
            ensureStarted()
            val host = postgresContainer.host
            val port = postgresContainer.getMappedPort(DB_PORT)
            val db = postgresContainer.databaseName
            val username = postgresContainer.username
            val password = postgresContainer.password
            val jdbcUrl = postgresContainer.jdbcUrl
            val r2dbcUrl = "r2dbc:postgresql://$host:$port/$db"

            // Wait for JDBC connection to become available to avoid race conditions
            waitForJdbc(jdbcUrl, username, password)

            registry.add("spring.r2dbc.url") { r2dbcUrl }
            registry.add("spring.r2dbc.username") { username }
            registry.add("spring.r2dbc.password") { password }

            // Liquibase and any JDBC usage expect a JDBC URL
            registry.add("spring.liquibase.url") { jdbcUrl }
            registry.add("spring.liquibase.user") { username }
            registry.add("spring.liquibase.password") { password }
            registry.add("spring.datasource.url") { jdbcUrl }
            registry.add("spring.datasource.username") { username }
            registry.add("spring.datasource.password") { password }
        }

        private fun waitForJdbc(
            jdbcUrl: String,
            username: String,
            password: String,
            timeoutSeconds: Long = DEFAULT_JDBC_TIMEOUT_SECONDS
        ) {
            val deadline = System.currentTimeMillis() + timeoutSeconds * MILLIS_PER_SECOND
            while (System.currentTimeMillis() < deadline) {
                try {
                    DriverManager.getConnection(jdbcUrl, username, password).use { _ ->
                        log.info("JDBC connection to $jdbcUrl successful")
                        return
                    }
                } catch (e: SQLException) {
                    log.info("Waiting for JDBC at $jdbcUrl...: ${e.message}")
                    Thread.sleep(SLEEP_INTERVAL_MILLIS)
                }
            }
            error(
                "Postgres did not become available at $jdbcUrl within $timeoutSeconds seconds",
            )
        }

        private fun registerMailProperties(registry: DynamicPropertyRegistry) {
            log.info("Registering Mail Properties")
            registry.add("spring.mail.host") { greenMailContainer.host }
            registry.add("spring.mail.port") { greenMailContainer.firstMappedPort }
        }

        private fun registerKeycloakProperties(registry: DynamicPropertyRegistry) {
            log.info("Registering Keycloak Properties")
            ensureStarted()
            val authServerUrl = removeLastSlash(keycloakContainer.authServerUrl)
            val issuerUri = "$authServerUrl/realms/$REALM"
            registry.add(
                "spring.security.oauth2.resourceserver.jwt.issuer-uri",
            ) { authServerUrl }

            // OAuth2 client provider configuration (used by SecurityConfiguration.jwtDecoder)
            registry.add(
                "spring.security.oauth2.client.provider.oidc.issuer-uri",
            ) { issuerUri }

            registry.add(
                "application.security.oauth2.base-url",
            ) { authServerUrl }

            registry.add(
                "application.security.oauth2.server-url",
            ) { authServerUrl }

            registry.add(
                "application.security.oauth2.issuer-uri",
            ) { issuerUri }

            registry.add(
                "application.security.oauth2.realm",
            ) { REALM }

            registry.add(
                "application.security.oauth2.client-id",
            ) { CLIENT_ID }

            registry.add(
                "application.security.oauth2.admin-client-id",
            ) { ADMIN_CLI }

            registry.add(
                "application.security.oauth2.admin-realm",
            ) { ADMIN_REALM }

            registry.add(
                "application.security.oauth2.admin-username",
            ) { ADMIN_USER }

            registry.add(
                "application.security.oauth2.admin-password",
            ) { ADMIN_PASSWORD }
        }

        @JvmStatic
        fun ensureStarted() {
            if (started.get()) return
            synchronized(this) {
                if (started.get()) return
                startInfrastructure()
                started.set(true)
            }
        }

        private fun removeLastSlash(url: String): String {
            if (url.length > 1 && url.endsWith("/")) {
                return url.substring(0, url.length - 1)
            }
            return url
        }

        @BeforeAll
        @JvmStatic
        fun startInfrastructure() {
            if (!postgresContainer.isRunning) {
                log.info("Postgres Container Start \uD83D\uDE80")
                postgresContainer.start()
            }
            if (!keycloakContainer.isRunning) {
                log.info("Keycloak Containers Start \uD83D\uDE80")
                keycloakContainer.start()
            }
            if (!greenMailContainer.isRunning) {
                log.info("Green Mail Containers Start \uD83D\uDCE8")
                greenMailContainer.start()
            }
        }
    }
}

private fun AccessTokenResponse.toAccessToken(): AccessToken =
    AccessToken(
        token = this.token,
        expiresIn = this.expiresIn,
        refreshToken = this.refreshToken,
        refreshExpiresIn = this.refreshExpiresIn,
        tokenType = this.tokenType,
        notBeforePolicy = this.notBeforePolicy,
        sessionState = this.sessionState,
        scope = this.scope,
    )
