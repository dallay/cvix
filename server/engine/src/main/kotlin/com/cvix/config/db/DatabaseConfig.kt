package com.cvix.config.db

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.reactive.TransactionalOperator

/**
 * R2DBC database configuration with Row-Level Security (RLS) support.
 *
 * This configuration wraps the auto-configured connection factory with
 * [WorkspaceConnectionFactoryDecorator] to automatically set the `cvix.current_workspace`
 * PostgreSQL session variable on each connection, enabling RLS policies to filter data
 * by workspace.
 *
 * ## How RLS Works
 *
 * 1. [com.cvix.config.WorkspaceContextWebFilter] extracts the workspace ID from the request
 * 2. [com.cvix.config.WorkspaceContextHolder] propagates the workspace ID through the reactive context
 * 3. [WorkspaceConnectionFactoryDecorator] sets `SET LOCAL cvix.current_workspace = '<id>'`
 * 4. PostgreSQL RLS policies use `current_setting('cvix.current_workspace')` to filter rows
 *
 * @see WorkspaceConnectionFactoryDecorator
 * @see com.cvix.config.WorkspaceContextHolder
 */
@Configuration
@EnableTransactionManagement
@EnableR2dbcRepositories(basePackages = ["com.cvix.*"])
@EnableR2dbcAuditing
class DatabaseConfig {

    /**
     * Provides the workspace-aware connection factory as the primary connection factory.
     *
     * This decorator intercepts connection creation to set the workspace session variable
     * from the reactive context, enabling Row-Level Security policies.
     *
     * The decorator wraps the auto-configured [ConnectionFactory] from Spring Boot's
     * R2DBC auto-configuration. By marking this as @Primary, all repositories and
     * database operations will use this workspace-aware version.
     *
     * @param connectionFactory The auto-configured connection factory from Spring Boot
     * @return A decorated connection factory that sets workspace context on each connection
     */
    @Bean
    @Primary
    @ConditionalOnBean(ConnectionFactory::class)
    fun workspaceAwareConnectionFactory(connectionFactory: ConnectionFactory): ConnectionFactory =
        WorkspaceConnectionFactoryDecorator(connectionFactory)

    /**
     * Transaction manager using the workspace-aware connection factory.
     *
     * Uses the @Primary workspace-aware connection factory to ensure all transactions
     * have the correct workspace context set.
     */
    @Bean
    @Primary
    fun connectionFactoryTransactionManager(
        workspaceAwareConnectionFactory: ConnectionFactory
    ): R2dbcTransactionManager = R2dbcTransactionManager(workspaceAwareConnectionFactory)

    /**
     * Transactional operator for programmatic transaction management in reactive flows.
     *
     * Use this when you need explicit transaction boundaries:
     * ```kotlin
     * transactionalOperator.executeAndAwait {
     *     repository.save(entity)
     * }
     * ```
     */
    @Bean
    fun transactionalOperator(txManager: R2dbcTransactionManager): TransactionalOperator =
        TransactionalOperator.create(txManager)
}
