package com.loomify.engine.config

import dasniko.testcontainers.keycloak.KeycloakContainer
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.time.Duration

/**
 * Singleton manager for Testcontainers to ensure containers are started once per test suite.
 * This prevents flakiness caused by container recreation and improves test performance.
 *
 * Key features:
 * - Lazy initialization: containers start only when first accessed
 * - Singleton pattern: containers shared across all tests
 * - Proper wait strategies: ensures containers are fully ready before tests run
 * - Container reuse: can be configured via testcontainers.properties
 * - Network isolation: all containers share a dedicated network
 */
object TestcontainersManager {
    private val log = LoggerFactory.getLogger(TestcontainersManager::class.java)

    private const val ADMIN_USER: String = "admin"
    private const val ADMIN_PASSWORD: String = "secret"
    private const val REALM: String = "loomify"
    private const val CLIENT_ID: String = "web_app"
    private const val ADMIN_CLI = "admin-cli"
    private const val ADMIN_REALM = "master"
    private const val WEB_PORT = 6080

    private val ports = arrayOf(3025, 3110, 3143, 3465, 3993, 3995, WEB_PORT)

    /**
     * Shared network for all containers to communicate with each other.
     * Using a singleton network prevents "network already exists" errors.
     */
    val network: Network by lazy {
        log.info("Creating shared test network...")
        Network.newNetwork()
    }

    /**
     * PostgreSQL container with optimized configuration for tests.
     * Uses the official PostgreSQL Alpine image for faster startup.
     */
    val postgresContainer: PostgreSQLContainer<*> by lazy {
        log.info("Starting PostgreSQL container...")
        try {
            PostgreSQLContainer(DockerImageName.parse("postgres:16.9-alpine"))
                .apply {
                    withNetwork(network)
                    withNetworkAliases("postgres")
                    withReuse(true)
                    withLabel("testcontainers.reuse.enable", "true")
                    // Optimize PostgreSQL for test performance
                    withCommand(
                        "postgres",
                        "-c", "fsync=off",
                        "-c", "synchronous_commit=off",
                        "-c", "full_page_writes=off"
                    )
                    start()
                    log.info("PostgreSQL container started successfully: {}", jdbcUrl)
                }
        } catch (e: Exception) {
            log.error("Failed to start PostgreSQL container", e)
            throw IllegalStateException("Could not start PostgreSQL container: ${e.message}", e)
        }
    }

    /**
     * Keycloak container with proper wait strategy.
     * Ensures Keycloak is fully initialized before tests run.
     * Uses the default wait strategy from KeycloakContainer.
     */
    val keycloakContainer: KeycloakContainer by lazy {
        log.info("Starting Keycloak container...")
        try {
            KeycloakContainer("keycloak/keycloak:25.0")
                .apply {
                    withRealmImportFile("keycloak/demo-realm-test.json")
                    withAdminUsername(ADMIN_USER)
                    withAdminPassword(ADMIN_PASSWORD)
                    withNetwork(network)
                    withNetworkAliases("keycloak")
                    withReuse(true)
                    withLabel("testcontainers.reuse.enable", "true")
                    withStartupTimeout(Duration.ofMinutes(5))
                    
                    start()
                    log.info("Keycloak container started successfully: {}", authServerUrl)
                }
        } catch (e: Exception) {
            log.error("Failed to start Keycloak container", e)
            throw IllegalStateException("Could not start Keycloak container: ${e.message}", e)
        }
    }

    /**
     * GreenMail container for email testing.
     * Configured with SMTP and IMAP protocols.
     */
    val greenMailContainer: GenericContainer<*> by lazy {
        log.info("Starting GreenMail container...")
        try {
            GenericContainer(DockerImageName.parse("greenmail/standalone:2.0.0"))
                .apply {
                    withEnv(
                        "GREENMAIL_OPTS",
                        "-Dgreenmail.setup.test.smtp -Dgreenmail.hostname=0.0.0.0"
                    )
                    withNetwork(network)
                    withNetworkAliases("greenmail")
                    withExposedPorts(*ports)
                    withReuse(true)
                    withLabel("testcontainers.reuse.enable", "true")
                    waitingFor(
                        Wait.forLogMessage(".*Starting GreenMail standalone.*", 1)
                            .withStartupTimeout(Duration.ofMinutes(2))
                    )
                    start()
                    log.info("GreenMail container started successfully on port: {}", firstMappedPort)
                }
        } catch (e: Exception) {
            log.error("Failed to start GreenMail container", e)
            throw IllegalStateException("Could not start GreenMail container: ${e.message}", e)
        }
    }

    /**
     * Returns Keycloak configuration for Spring properties.
     */
    fun getKeycloakProperties(): Map<String, String> {
        val authServerUrl = removeLastSlash(keycloakContainer.authServerUrl)
        return mapOf(
            "spring.security.oauth2.resourceserver.jwt.issuer-uri" to authServerUrl,
            "application.security.oauth2.base-url" to authServerUrl,
            "application.security.oauth2.server-url" to authServerUrl,
            "application.security.oauth2.issuer-uri" to "$authServerUrl/realms/$REALM",
            "application.security.oauth2.realm" to REALM,
            "application.security.oauth2.client-id" to CLIENT_ID,
            "application.security.oauth2.admin-client-id" to ADMIN_CLI,
            "application.security.oauth2.admin-realm" to ADMIN_REALM,
            "application.security.oauth2.admin-username" to ADMIN_USER,
            "application.security.oauth2.admin-password" to ADMIN_PASSWORD
        )
    }

    /**
     * Returns GreenMail configuration for Spring properties.
     */
    fun getMailProperties(): Map<String, String> = mapOf(
        "spring.mail.host" to greenMailContainer.host,
        "spring.mail.port" to greenMailContainer.firstMappedPort.toString()
    )

    /**
     * Returns PostgreSQL configuration for Spring properties.
     */
    fun getPostgresProperties(): Map<String, String> = mapOf(
        "spring.r2dbc.url" to postgresContainer.jdbcUrl.replace("jdbc:", "r2dbc:"),
        "spring.r2dbc.username" to postgresContainer.username,
        "spring.r2dbc.password" to postgresContainer.password
    )

    /**
     * Ensures all containers are started and ready.
     * This method is idempotent - calling it multiple times has no effect.
     */
    fun startAll() {
        log.info("Ensuring all test containers are started...")
        try {
            // Access lazy properties to trigger initialization
            // Each property has its own error handling, but we catch any unexpected errors here
            postgresContainer
            keycloakContainer
            greenMailContainer
            log.info("All test containers are ready!")
        } catch (e: Exception) {
            log.error("Failed to start test containers", e)
            throw IllegalStateException("Could not initialize test containers: ${e.message}", e)
        }
    }

    private fun removeLastSlash(url: String): String {
        if (url.length > 1 && url.endsWith("/")) {
            return url.substring(0, url.length - 1)
        }
        return url
    }
}
