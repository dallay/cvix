package com.cvix.workspace.infrastructure.event.consumer

import com.cvix.common.domain.bus.Mediator
import com.cvix.common.domain.bus.command.CommandHandlerExecutionError
import com.cvix.common.domain.bus.event.EventConsumer
import com.cvix.common.domain.bus.event.Subscribe
import com.cvix.users.domain.event.UserCreatedEvent
import com.cvix.workspace.application.create.CreateWorkspaceCommand
import com.cvix.workspace.domain.Workspace
import io.r2dbc.spi.R2dbcException
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

/**
 * Event consumer that automatically creates a default workspace when a new user is created.
 *
 * This component listens to UserCreatedEvent and creates a default workspace for the user
 * using deterministic workspace IDs to prevent race conditions. This ensures that every
 * user has a workspace to work with from the moment they register.
 *
 * @property mediator Mediator to dispatch CreateWorkspaceCommand
 */
@Component
@Subscribe(filterBy = UserCreatedEvent::class)
class CreateDefaultWorkspaceOnUserCreation(
    private val mediator: Mediator
) : EventConsumer<UserCreatedEvent> {

    /**
     * Handles UserCreatedEvent by creating a default workspace with deterministic ID.
     * Uses deterministic workspace IDs to prevent race conditions and relies on database
     * uniqueness constraints to handle concurrent creation attempts gracefully.
     *
     * @param event The UserCreatedEvent containing user information
     */
    override suspend fun consume(event: UserCreatedEvent) {
        log.debug("Processing user creation event for user: {}", event.id)

        try {
            log.debug("Creating default workspace for user: {}", event.id)

            // Use deterministic workspace ID based on user ID to prevent race conditions
            val workspaceId = UUID.nameUUIDFromBytes("default-workspace:${event.id}".toByteArray())
            val workspaceName = generateDefaultWorkspaceName(event.firstName, event.lastName)

            val createWorkspaceCommand = CreateWorkspaceCommand(
                id = workspaceId,
                name = workspaceName,
                description = "Default workspace created automatically upon user registration",
                ownerId = event.id,
                isDefault = true,
            )

            mediator.send(createWorkspaceCommand)
            log.debug(
                "Successfully created default workspace '{}' with id {} for user: {}",
                workspaceName,
                workspaceId,
                event.id,
            )
        } catch (e: CommandHandlerExecutionError) {
            log.error(
                "Failed to create default workspace for user: {} due to command execution error. $NO_WORKSPACE_MESSAGE",
                event.id,
                e,
            )
        } catch (e: ResponseStatusException) {
            log.error(
                "Failed to create default workspace for user: {} due to validation error. $NO_WORKSPACE_MESSAGE",
                event.id,
                e,
            )
        } catch (e: IllegalArgumentException) {
            log.error(
                "Failed to create default workspace for user: {} due to invalid input. $NO_WORKSPACE_MESSAGE",
                event.id,
                e,
            )
        } catch (e: DataIntegrityViolationException) {
            log.error(
                "Failed to create default workspace for user: {} due to data integrity " +
                    "violation. $NO_WORKSPACE_MESSAGE",
                event.id,
                e,
            )
        } catch (e: R2dbcException) {
            log.error(
                "Failed to create default workspace for user: {} due to database error. $NO_WORKSPACE_MESSAGE",
                event.id,
                e,
            )
        } catch (@SuppressWarnings("TooGenericExceptionCaught") e: Exception) {
            // Catch any unexpected exceptions to ensure the user account remains valid
            // and tests expecting graceful handling do not fail due to uncaught exceptions.
            log.error(
                "Failed to create default workspace for user: {} due to unexpected error. $NO_WORKSPACE_MESSAGE",
                event.id,
                e,
            )
        }
    }

    /**
     * Generates a default workspace name based on user information.
     *
     * @param firstname User's first name (optional)
     * @param lastname User's last name (optional)
     * @return Generated workspace name
     */
    private fun generateDefaultWorkspaceName(firstname: String?, lastname: String?): String {
        val maxLength = Workspace.NAME_MAX_LENGTH
        val composedName = when {
            !firstname.isNullOrBlank() && !lastname.isNullOrBlank() ->
                "${firstname.trim()} ${lastname.trim()}'s Workspace"
            !firstname.isNullOrBlank() ->
                "${firstname.trim()}'s Workspace"
            !lastname.isNullOrBlank() ->
                "${lastname.trim()}'s Workspace"
            else ->
                "My Workspace"
        }
        return if (composedName.length > maxLength) composedName.take(maxLength) else composedName
    }

    companion object {
        private val log = LoggerFactory.getLogger(CreateDefaultWorkspaceOnUserCreation::class.java)
        @SuppressWarnings("MultilineRawStringIndentation")
        private val NO_WORKSPACE_MESSAGE = """
           User account will remain valid but without a workspace.
           The user will need to manually create a workspace via the application interface or API before accessing workspace-related features.
           Administrators may assist by creating a workspace for the user if needed.
           Refer to the documentation or contact support for further guidance.
        """.trimIndent()
    }
}
