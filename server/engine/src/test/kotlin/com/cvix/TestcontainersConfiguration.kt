package com.cvix

import com.cvix.common.domain.SystemEnvironment.getEnvOrDefault
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Test configuration for Testcontainers.
 * 
 * Used by:
 * - TestHatchgridApplication.kt for local development with containers
 * - Integration tests via @ServiceConnection auto-configuration
 * 
 * Supports environment variables with fallback to defaults:
 * - POSTGRESQL_VERSION (default: 17-alpine)
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> =
        PostgreSQLContainer(
            DockerImageName.parse("postgres:${getEnvOrDefault("POSTGRESQL_VERSION", "17-alpine")}")
        )
}
