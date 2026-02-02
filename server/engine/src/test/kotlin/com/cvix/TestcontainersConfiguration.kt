package com.cvix

import com.cvix.common.util.SystemEnvironment.getEnvOrDefault
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Test configuration for Testcontainers.
 *
 * Used by:
 * - TestApplication.kt for local development with containers
 * - Integration tests via @ServiceConnection auto-configuration
 *
 * Supports environment variables with fallback to defaults:
 * - POSTGRESQL_VERSION (default: 18.1)
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> =
        PostgreSQLContainer(
            DockerImageName.parse("postgres:${getEnvOrDefault("POSTGRESQL_VERSION", "18.1")}"),
        )

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()
}
