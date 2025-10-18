package com.loomify.engine.config.db

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
@EnableTransactionManagement
@EnableR2dbcRepositories(basePackages = ["com.loomify.*"])
@EnableR2dbcAuditing
class DatabaseConfig {
    @Bean
    fun transactionalOperator(connectionFactory: ConnectionFactory): TransactionalOperator =
        TransactionalOperator.create(R2dbcTransactionManager(connectionFactory))
}
