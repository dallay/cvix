package com.cvix.workspace.application.create

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandHandler
import com.cvix.workspace.domain.Workspace
import com.cvix.workspace.domain.WorkspaceException
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
) : CommandHandler<CreateWorkspaceCommand> {

    /**
     * Handles the creation of a workspace.
     * It validates the command, creates the workspace domain entity, and delegates
     * to the WorkspaceCreator service which handles idempotency and persistence.
     *
     * @param command The [CreateWorkspaceCommand] containing the information needed to create a workspace.
     */
    override suspend fun handle(command: CreateWorkspaceCommand) {
        require(command.name.isNotBlank()) { "Workspace name cannot be blank" }

        log.debug("Creating workspace with name: ${command.name}, isDefault: ${command.isDefault}")

        try {
            val workspace = Workspace.create(
                id = command.id,
                name = command.name,
                description = command.description,
                ownerId = command.ownerId,
                isDefault = command.isDefault,
            )

            val created = workspaceCreator.create(workspace)

            if (created) {
                log.info("Successfully created workspace with id: ${command.id}")
            } else {
                log.info("Workspace creation skipped for id: ${command.id} (duplicate default workspace)")
            }
        } catch (exception: IllegalArgumentException) {
            log.error("Invalid UUID format in create workspace command: ${exception.message}")
            throw IllegalArgumentException("Invalid workspace or owner ID format", exception)
        } catch (exception: Exception) {
            // Fallback: For default workspaces, if we still hit a duplicate error (extreme race condition),
            // treat it as success since another concurrent request already created the workspace
            if (command.isDefault && isDuplicateDefaultWorkspaceError(exception)) {
                log.info(
                    "Race condition detected: default workspace already created for user ${command.ownerId} " +
                        "after initial check, treating as success",
                )
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
    }
}
