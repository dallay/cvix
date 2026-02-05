package com.cvix.config

import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.DriverManagerDataSource

/**
 * Test configuration providing JDBC DataSource for @Sql annotations.
 *
 * Even though the application uses R2DBC, tests with @Sql annotations need a JDBC DataSource
 * to execute SQL scripts before/after test methods. The dynamic properties from
 * InfrastructureTestContainers provide the connection details.
 *
 * This configuration is automatically imported by [IntegrationTest] annotation.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestDataSourceConfiguration {

    /**
     * JDBC DataSource bean required by @Sql test annotations.
     *
     * Uses the dynamic properties set by InfrastructureTestContainers:
     * - spring.datasource.url
     * - spring.datasource.username
     * - spring.datasource.password
     */
    @Bean
    fun dataSource(
        @Value("\${spring.datasource.url}") url: String,
        @Value("\${spring.datasource.username}") username: String,
        @Value("\${spring.datasource.password}") password: String,
    ): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName("org.postgresql.Driver")
        dataSource.url = url
        dataSource.username = username
        dataSource.password = password
        return dataSource
    }
}
