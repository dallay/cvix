package com.loomify.engine

import com.loomify.engine.config.TestcontainersManager
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer

/**
 * Test configuration that provides PostgreSQL container as a Spring bean.
 * Uses the singleton container from TestcontainersManager to ensure
 * the same container instance is shared across all tests.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> = TestcontainersManager.postgresContainer
}
