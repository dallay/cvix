package com.cvix.config.db

import com.cvix.config.WorkspaceContextHolder
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

private val log: Logger = LoggerFactory.getLogger(WorkspaceConnectionFactoryDecorator::class.java)

/**
 * A decorator for [ConnectionFactory] that sets the PostgreSQL session variable
 * `cvix.current_workspace` on each acquired connection based on the reactive context.
 *
 * This enables Row-Level Security (RLS) policies that filter data by workspace.
 *
 * ## How It Works
 *
 * 1. When a connection is acquired from the pool, this decorator checks the
 *    reactive context for a workspace ID via [com.cvix.config.WorkspaceContextHolder]
 *
 * 2. If a workspace ID is present, it executes:
 *    ```sql
 *    SET LOCAL cvix.current_workspace = '<workspace-uuid>'
 *    ```
 *
 * 3. `SET LOCAL` ensures the variable is scoped to the current transaction,
 *    automatically clearing when the transaction completes (no manual cleanup needed)
 *
 * ## Why SET LOCAL?
 *
 * - `SET LOCAL` scopes the variable to the current transaction
 * - When the transaction ends (commit/rollback), the variable is automatically reset
 * - This prevents state leakage between different tenant contexts
 * - No explicit cleanup is needed when returning connections to the pool
 *
 * ## Usage
 *
 * This decorator is automatically applied in [DatabaseConfig]:
 *
 * ```kotlin
 * @Bean
 * fun workspaceAwareConnectionFactory(
 *     connectionFactory: ConnectionFactory
 * ): ConnectionFactory = WorkspaceConnectionFactoryDecorator(connectionFactory)
 * ```
 *
 * @param delegate The underlying connection factory to wrap
 * @see com.cvix.config.WorkspaceContextHolder
 */
class WorkspaceConnectionFactoryDecorator(
    private val delegate: ConnectionFactory
) : ConnectionFactory {

    override fun create(): Publisher<out Connection> {
        return Mono.from(delegate.create())
            .flatMap { connection ->
                WorkspaceContextHolder.getWorkspaceId()
                    .flatMap { workspaceId ->
                        setWorkspaceOnConnection(connection, workspaceId)
                            .thenReturn(connection)
                    }
                    .defaultIfEmpty(connection)
                    .onErrorResume { error ->
                        log.warn("Failed to set workspace context on connection, proceeding without RLS context", error)
                        Mono.just(connection)
                    }
            }
    }

    override fun getMetadata(): ConnectionFactoryMetadata = delegate.metadata

    /**
     * Sets the workspace session variable on the given connection.
     *
     * @param connection The R2DBC connection
     * @param workspaceId The workspace ID to set
     * @return A Mono that completes when the variable is set
     */
    private fun setWorkspaceOnConnection(
        connection: Connection,
        workspaceId: java.util.UUID
    ): Mono<Void> {
        val sql = SET_WORKSPACE_SQL.format(workspaceId.toString())
        if (log.isDebugEnabled) {
            log.debug("Setting workspace context: {} = {}", WORKSPACE_SESSION_VARIABLE, workspaceId)
        }

        return Mono.from(connection.createStatement(sql).execute())
            .flatMap { result ->
                Mono.from(result.rowsUpdated)
            }
            .doOnSuccess {
                if (log.isTraceEnabled) {
                    log.trace("Workspace context set successfully for workspace: {}", workspaceId)
                }
            }
            .then()
    }

    companion object {
        /**
         * PostgreSQL session variable name for the current workspace.
         * This must match the variable used in RLS policies.
         */
        const val WORKSPACE_SESSION_VARIABLE = "cvix.current_workspace"

        /**
         * SQL statement template for setting the workspace session variable.
         * Uses SET LOCAL to scope the variable to the current transaction.
         */
        private const val SET_WORKSPACE_SQL = "SET LOCAL $WORKSPACE_SESSION_VARIABLE = '%s'"
    }
}
