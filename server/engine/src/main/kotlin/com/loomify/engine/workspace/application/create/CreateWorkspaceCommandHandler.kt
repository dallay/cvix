package com.loomify.engine.workspace.application.create

import com.loomify.common.domain.Service
import com.loomify.common.domain.bus.command.CommandHandler
import com.loomify.engine.workspace.domain.Workspace
import com.loomify.engine.workspace.domain.WorkspaceException
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import java.util.*
import org.slf4j.LoggerFactory

/**
 * [CreateWorkspaceCommandHandler] is a class responsible for handling the creation of workspace.
 * It implements the [CommandHandler] interface with [CreateWorkspaceCommand] as the command type.
 *
 * @property workspaceCreator The [WorkspaceCreator] used to create workspace.
 */
@Service
class CreateWorkspaceCommandHandler(
    private val workspaceCreator: WorkspaceCreator,
    private val meterRegistry: MeterRegistry
) : CommandHandler<CreateWorkspaceCommand> {
    private val dupDefaultWsIgnoredCounter by lazy {
        Counter
            .builder(METRIC_WS_DEFAULT_DUP_IGN)
            .description("Count of ignored duplicate default workspace creations")
            .register(meterRegistry)
    }

    /**
     * Handles the creation of a workspace.
     * It logs the creation process, creates a new workspace using the [WorkspaceCreator],
     * and then creates the workspace. For default workspaces, it handles duplicate insertions
     * gracefully to prevent race conditions.
     *
     * @param command The [CreateWorkspaceCommand] containing the information needed to create a workspace.
     */
    override suspend fun handle(command: CreateWorkspaceCommand) {
        require(command.name.isNotBlank()) { "Workspace name cannot be blank" }

        log.debug("Creating workspace with name: ${command.name}, isDefault: ${command.isDefault}")
        try {
            val workspaceId = command.id
            val ownerId = command.ownerId

            val workspace = Workspace.create(
                id = workspaceId,
                name = command.name,
                description = command.description,
                ownerId = ownerId,
                isDefault = command.isDefault,
            )
            workspaceCreator.create(workspace)
            log.info("Successfully created workspace with id: ${command.id}")
        } catch (exception: IllegalArgumentException) {
            log.error("Invalid UUID format in create workspace command: ${exception.message}")
            throw IllegalArgumentException("Invalid workspace or owner ID format", exception)
        } catch (exception: Exception) {
            // For default workspaces, check if this is a duplicate insertion due to race condition
            if (command.isDefault && isDuplicateDefaultWorkspaceError(exception)) {
                dupDefaultWsIgnoredCounter.increment()
                log.info("Default workspace already exists for user ${command.ownerId}, ignoring duplicate creation")
                return
            }
            log.error("Failed to create workspace with name: ${command.name}", exception)
            throw WorkspaceException("Error creating workspace", exception)
        }
    }

    /**
     * Checks if the exception indicates a duplicate default workspace insertion.
     * This helps handle race conditions gracefully.
     */
    private fun isDuplicateDefaultWorkspaceError(exception: Exception): Boolean {
        // 1) Prefer SQLSTATE inspection for robust detection
        val sqlState = extractSqlState(exception)
        if (sqlState == "23505") return true // unique_violation

        // 2) Spring's translated exceptions
        if (exception is org.springframework.dao.DuplicateKeyException) return true

        // 3) Fallback to message/index name checks through the cause chain
        val allMessages =
            generateSequence(exception as? Throwable) { it.cause }
                .mapNotNull { it.message?.lowercase() }
                .joinToString(" | ")

        return allMessages.contains("idx_workspaces_owner_default") ||
            allMessages.contains("duplicate key") ||
            allMessages.contains("unique constraint") ||
            allMessages.contains("duplicate")
    }

    private fun extractSqlState(throwable: Throwable): String? {
        var current: Throwable? = throwable
        while (current != null) {
            when (current) {
                is io.r2dbc.spi.R2dbcException -> return current.sqlState
                // Defensive: support JDBC style if ever encountered
                is java.sql.SQLException -> return current.sqlState
            }
            current = current.cause
        }
        return null
    }

    companion object {
        private val log = LoggerFactory.getLogger(CreateWorkspaceCommandHandler::class.java)
        const val METRIC_WS_DEFAULT_DUP_IGN = "workspace.default.duplicate.ignored"
    }
}
