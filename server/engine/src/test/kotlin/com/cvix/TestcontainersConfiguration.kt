package com.cvix

import com.cvix.common.util.SystemEnvironment.getEnvOrDefault
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Configuration for running the application locally with Testcontainers.
 *
 * Used ONLY by:
 * - TestApplication.kt for local development with containers
 *
 * NOT used by integration tests - those use InfrastructureTestContainers
 * which manages all test containers and their lifecycle.
 *
 * Note: This configuration requires the "local" profile to be active,
 * preventing it from being loaded during integration tests (which use "test" profile).
 *
 * Supports environment variables with fallback to defaults:
 * - POSTGRESQL_VERSION (default: 18.1)
 */
@Configuration(proxyBeanMethods = false)
@Profile("local")
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
