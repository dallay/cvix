package com.cvix.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.DriverManagerDataSource

/**
 * Provide a JDBC DataSource bean for tests that use @Sql annotations.
 * The JDBC URL and credentials are provided via dynamic properties registered by
 * InfrastructureTestContainers (spring.datasource.url, username, password).
 */
@TestConfiguration(proxyBeanMethods = false)
class TestDatabaseConfiguration {
    @Bean
    fun dataSource(
        @Value("\${spring.datasource.url}") jdbcUrl: String,
        @Value("\${spring.datasource.username}") username: String,
        @Value("\${spring.datasource.password}") password: String,
    ): DriverManagerDataSource {
        val ds = DriverManagerDataSource()
        ds.setDriverClassName("org.postgresql.Driver")
        ds.url = jdbcUrl
        ds.username = username
        ds.password = password
        return ds
    }
}
