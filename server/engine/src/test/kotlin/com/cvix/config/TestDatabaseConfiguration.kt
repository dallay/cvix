package com.cvix.config

import com.cvix.common.domain.SystemEnvironment.getEnvOrDefault
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Test configuration for database integration tests.
 * 
 * Purpose:
 * - Provides PostgreSQL container for integration tests
 * - Creates JDBC DataSource bean required by @Sql test annotations
 * - @ServiceConnection auto-configures R2DBC connection for reactive repositories
 * 
 * Why separate from TestcontainersConfiguration?
 * - TestcontainersConfiguration: Used by TestHatchgridApplication for local dev
 * - TestDatabaseConfiguration: Used by integration tests needing JDBC DataSource
 * 
 * Note: Tests using @Sql annotations require a JDBC DataSource, even though
 * the application uses R2DBC. This configuration provides both:
 * - R2DBC connection via @ServiceConnection (for application code)
 * - JDBC DataSource (for @Sql test utilities)
 * 
 * Supports environment variables with fallback to defaults:
 * - POSTGRESQL_VERSION (default: 17-alpine)
 */
@TestConfiguration(proxyBeanMethods = false)
class TestDatabaseConfiguration {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> =
        PostgreSQLContainer(
            DockerImageName.parse("postgres:${getEnvOrDefault("POSTGRESQL_VERSION", "17-alpine")}")
        )
            .withUsername("test")
            .withPassword("test")

    /**
     * JDBC DataSource bean required by @Sql test annotations.
     * 
     * Even though the application uses R2DBC, tests with @Sql need a JDBC DataSource
     * to execute SQL scripts before/after test methods.
     */
    @Bean
    fun dataSource(postgres: PostgreSQLContainer<*>): DriverManagerDataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName("org.postgresql.Driver")
        dataSource.url = postgres.jdbcUrl
        dataSource.username = postgres.username
        dataSource.password = postgres.password
        return dataSource
    }
}
