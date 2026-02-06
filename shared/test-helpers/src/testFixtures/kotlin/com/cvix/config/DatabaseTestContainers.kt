package com.cvix.config

import com.cvix.IntegrationTest
import com.cvix.common.util.SystemEnvironment.getEnvOrDefault
import java.sql.DriverManager
import java.sql.SQLException
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.jupiter.api.BeforeAll
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

private const val DB_PORT = 5432

/**
 * Lightweight base class for integration tests that only need a PostgreSQL database.
 *
 * Use this instead of [InfrastructureTestContainers] when the test does NOT require
 * Keycloak or GreenMail (e.g., tests using [TestSecurityConfiguration] for mock OAuth2 beans).
 * This avoids resource contention from running multiple Keycloak instances in CI.
 */
@IntegrationTest
abstract class DatabaseTestContainers {
    @Value("\${testing.security.username}")
    protected lateinit var testUsername: String

    @Value("\${testing.security.password}")
    protected lateinit var testPassword: String

    init {
        log.info("Starting database infrastructure... 🐘")
        ensureStarted()
    }

    companion object {
        private val log = LoggerFactory.getLogger(DatabaseTestContainers::class.java)

        /**
         * Unique suffix for test container names. Can be overridden via system property
         * `tc.name.suffix` to support container reuse across test runs.
         */
        private val uniqueTestSuffix: String by lazy {
            System.getProperty("tc.name.suffix") ?: UUID.randomUUID().toString()
        }

        // Named constants to avoid magic numbers in the wait logic
        private const val DEFAULT_JDBC_TIMEOUT_SECONDS: Long = 60
        private const val SLEEP_INTERVAL_MILLIS: Long = 1000
        private const val MILLIS_PER_SECOND: Long = 1000

        // Ensure containers are started once in a thread-safe manner
        private val started = AtomicBoolean(false)

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
            .withCreateContainerCmdModifier { cmd -> cmd.withName("postgres-db-tests-$uniqueTestSuffix") }
            // Increase startup timeout for CI environments with limited I/O
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(180)))

        @JvmStatic
        @DynamicPropertySource
        fun registerDatabaseProperties(registry: DynamicPropertyRegistry) {
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
            timeoutSeconds: Long = DEFAULT_JDBC_TIMEOUT_SECONDS,
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

        @JvmStatic
        fun ensureStarted() {
            if (started.get()) return
            synchronized(this) {
                if (started.get()) return
                startDatabase()
                started.set(true)
            }
        }

        @BeforeAll
        @JvmStatic
        fun startDatabase() {
            if (!postgresContainer.isRunning) {
                log.info("Postgres Container Start 🚀")
                postgresContainer.start()
            }
        }
    }
}
