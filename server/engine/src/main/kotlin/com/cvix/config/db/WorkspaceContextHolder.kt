package com.cvix.config.db

import java.util.UUID
import reactor.core.publisher.Mono
import reactor.util.context.Context

/**
 * Provides reactive context propagation for the current workspace ID.
 *
 * This is used to propagate the workspace context through the reactive chain,
 * enabling Row-Level Security (RLS) policies in PostgreSQL by setting the
 * `cvix.current_workspace` session variable on each database connection.
 *
 * ## Usage
 *
 * ### Setting the workspace context:
 * ```kotlin
 * someReactiveOperation()
 *     .contextWrite(WorkspaceContextHolder.withWorkspace(workspaceId))
 * ```
 *
 * ### Reading the workspace context:
 * ```kotlin
 * WorkspaceContextHolder.getWorkspaceId()
 *     .flatMap { workspaceId -> doSomethingWith(workspaceId) }
 * ```
 *
 * @see WorkspaceConnectionFactoryDecorator
 */
object WorkspaceContextHolder {

    /**
     * Context key for storing the workspace ID in the reactive context.
     *
     * IMPORTANT: This key is shared with ApiController.WORKSPACE_CONTEXT_KEY
     * in shared/spring-boot-common. Both must use the same value.
     */
    private const val WORKSPACE_CONTEXT_KEY = "com.cvix.config.db.WorkspaceContextHolder.WORKSPACE_ID"

    /**
     * Creates a context modifier that sets the workspace ID.
     *
     * @param workspaceId The workspace ID to set in the context
     * @return A function that modifies the context to include the workspace ID
     */
    fun withWorkspace(workspaceId: UUID): (Context) -> Context = { context ->
        context.put(WORKSPACE_CONTEXT_KEY, workspaceId)
    }

    /**
     * Creates a context modifier that sets the workspace ID from a string.
     *
     * @param workspaceId The workspace ID as a string
     * @return A function that modifies the context to include the workspace ID
     * @throws IllegalArgumentException if the string is not a valid UUID
     */
    fun withWorkspace(workspaceId: String): (Context) -> Context =
        withWorkspace(UUID.fromString(workspaceId))

    /**
     * Retrieves the current workspace ID from the reactive context.
     *
     * @return A Mono containing the workspace ID, or empty if not set
     */
    fun getWorkspaceId(): Mono<UUID> = Mono.deferContextual { contextView ->
        if (contextView.hasKey(WORKSPACE_CONTEXT_KEY)) {
            Mono.just(contextView.get<UUID>(WORKSPACE_CONTEXT_KEY))
        } else {
            Mono.empty()
        }
    }

    /**
     * Retrieves the current workspace ID from the reactive context, or throws if not set.
     *
     * @return A Mono containing the workspace ID
     * @throws IllegalStateException if the workspace ID is not set in the context
     */
    fun requireWorkspaceId(): Mono<UUID> = getWorkspaceId()
        .switchIfEmpty(
            Mono.error(
                IllegalStateException(
                    "Workspace ID not found in reactive context. " +
                        "Ensure the request has a valid workspace context.",
                ),
            ),
        )

    /**
     * Clears the workspace context.
     *
     * @return A function that removes the workspace ID from the context
     */
    fun clear(): (Context) -> Context = { context ->
        context.delete(WORKSPACE_CONTEXT_KEY)
    }
}
