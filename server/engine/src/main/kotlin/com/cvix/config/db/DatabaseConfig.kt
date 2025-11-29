package com.cvix.config.db

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
@EnableTransactionManagement
@EnableR2dbcRepositories(basePackages = ["com.cvix.*"])
@EnableR2dbcAuditing
class DatabaseConfig(
    private val connectionFactory: ConnectionFactory
) : AbstractR2dbcConfiguration() {

    override fun connectionFactory(): ConnectionFactory = connectionFactory

    override fun getCustomConverters(): List<Any> = emptyList()
    // Note: We use io.r2dbc.postgresql.codec.Json directly for JSONB columns
    // Custom converters are not needed and can interfere with native type handling

    @Bean
    fun connectionFactoryTransactionManager(): R2dbcTransactionManager =
        R2dbcTransactionManager(connectionFactory)

    @Bean
    fun transactionalOperator(txManager: R2dbcTransactionManager): TransactionalOperator =
        TransactionalOperator.create(txManager)
}
