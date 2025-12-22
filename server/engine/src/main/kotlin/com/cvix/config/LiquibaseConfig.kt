package com.cvix.config

import javax.sql.DataSource
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

/**
 * Configuration for Liquibase database migrations with R2DBC.
 *
 * Spring Boot's R2DBC auto-configuration backs off the JDBC DataSource auto-configuration,
 * which means Liquibase has no DataSource to use for migrations by default.
 *
 * This configuration creates a JDBC DataSource for Liquibase by deriving it from the R2DBC URL.
 * It converts `r2dbc:postgresql://host:port/db` to `jdbc:postgresql://host:port/db`.
 */
@Configuration
@EnableConfigurationProperties(LiquibaseProperties::class)
class LiquibaseConfig(
    private val environment: Environment,
) {
    private val logger = LoggerFactory.getLogger(LiquibaseConfig::class.java)

    /**
     * Creates a JDBC DataSource for Liquibase migrations.
     *
     * This bean is only created when no other DataSource is available (e.g., in tests
     * where Testcontainers provides the DataSource).
     *
     * Priority order:
     * 1. LIQUIBASE_URL environment variable (if explicitly set)
     * 2. Derived from DATABASE_URL (spring.r2dbc.url) by converting r2dbc: to jdbc:
     */
    @Bean
    @LiquibaseDataSource
    @ConditionalOnMissingBean(DataSource::class)
    fun liquibaseDataSource(): DataSource {
        val r2dbcUrl = checkNotNull(environment.getProperty("spring.r2dbc.url")) {
            "spring.r2dbc.url is not configured"
        }

        val jdbcUrl = environment.getProperty("LIQUIBASE_URL")
            ?: convertR2dbcToJdbc(r2dbcUrl)

        val username = checkNotNull(environment.getProperty("spring.r2dbc.username")) {
            "spring.r2dbc.username is not configured"
        }

        val password = checkNotNull(environment.getProperty("spring.r2dbc.password")) {
            "spring.r2dbc.password is not configured"
        }

        logger.info("Configuring Liquibase DataSource with JDBC URL: {}", jdbcUrl)

        return DataSourceBuilder.create()
            .url(jdbcUrl)
            .username(username)
            .password(password)
            .driverClassName("org.postgresql.Driver")
            .build()
    }

    /**
     * Converts an R2DBC URL to a JDBC URL.
     *
     * Examples:
     * - r2dbc:postgresql://localhost:5432/mydb → jdbc:postgresql://localhost:5432/mydb
     * - r2dbc:pool:postgresql://localhost:5432/mydb → jdbc:postgresql://localhost:5432/mydb
     */
    private fun convertR2dbcToJdbc(r2dbcUrl: String): String {
        // Remove r2dbc: or r2dbc:pool: prefix
        val withoutR2dbc = r2dbcUrl
            .removePrefix("r2dbc:pool:")
            .removePrefix("r2dbc:")

        // Add jdbc: prefix
        return "jdbc:$withoutR2dbc"
    }
}
