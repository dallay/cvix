package com.cvix.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestDatabaseConfiguration {
    
    companion object {
        /**
         * Retrieves environment variable or returns default value
         */
        private fun getEnvOrDefault(key: String, default: String): String =
            System.getenv(key) ?: default
    }
    
    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> =
        PostgreSQLContainer(DockerImageName.parse("postgres:${getEnvOrDefault("POSTGRESQL_VERSION", "17-alpine")}"))
            .withUsername("test")
            .withPassword("test")

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
