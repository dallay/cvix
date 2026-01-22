package com.cvix.config.db

import com.cvix.config.WorkspaceContextHolder
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.reactor.ReactorContext
import reactor.util.context.Context

/**
 * Coroutine context element for propagating workspace ID through suspend functions.
 *
 * This works in conjunction with [com.cvix.config.WorkspaceContextHolder] to enable Row-Level Security (RLS)
 * in a coroutine-based application. While [com.cvix.config.WorkspaceContextHolder] uses Reactor's Context,
 * this element bridges the gap for suspend functions.
 *
 * ## Usage in Suspend Functions
 *
 * ### Setting the workspace context:
 * ```kotlin
 * withContext(WorkspaceCoroutineContext(workspaceId)) {
 *     // All database operations here will have RLS applied
 *     resumeRepository.save(resume)
 * }
 * ```
 *
 * ### Or using the extension function:
 * ```kotlin
 * withWorkspaceContext(workspaceId) {
 *     resumeRepository.save(resume)
 * }
 * ```
 *
 * ### Getting the current workspace:
 * ```kotlin
 * val workspaceId = coroutineContext.workspaceId
 * ```
 *
 * ## How It Works
 *
 * 1. [WorkspaceCoroutineContext] stores the workspace ID in the coroutine context
 * 2. When a coroutine suspends and resumes on a reactive operator, the [ReactorContext]
 *    is synchronized with the coroutine context
 * 3. [WorkspaceConnectionFactoryDecorator] reads from either:
 *    - The Reactor context (via [com.cvix.config.WorkspaceContextHolder])
 *    - The coroutine context (via [WorkspaceCoroutineContext])
 *
 * @property workspaceId The workspace UUID to propagate
 * @see com.cvix.config.WorkspaceContextHolder
 * @see WorkspaceConnectionFactoryDecorator
 */
data class WorkspaceCoroutineContext(
    val workspaceId: UUID
) : CoroutineContext.Element {

    override val key: CoroutineContext.Key<*> = Key

    /**
     * Converts this coroutine context element to a Reactor Context modifier.
     * This allows seamless integration between coroutine and reactive contexts.
     */
    fun toReactorContextModifier(): (Context) -> Context =
        WorkspaceContextHolder.withWorkspace(workspaceId)

    companion object Key : CoroutineContext.Key<WorkspaceCoroutineContext>
}

/**
 * Extension property to get the workspace ID from the current coroutine context.
 *
 * @return The workspace UUID if present, null otherwise
 */
val CoroutineContext.workspaceId: UUID?
    get() = this[WorkspaceCoroutineContext]?.workspaceId

/**
 * Extension property to require the workspace ID from the current coroutine context.
 *
 * @return The workspace UUID
 * @throws IllegalStateException if no workspace context is set
 */
val CoroutineContext.requireWorkspaceId: UUID
    get() = workspaceId ?: throw IllegalStateException(
        "Workspace ID not found in coroutine context. " +
            "Ensure the operation is wrapped with withWorkspaceContext()",
    )

/**
 * Executes the given block with workspace context.
 *
 * This sets up both the coroutine context and Reactor context to ensure
 * RLS is properly applied for all database operations within the block.
 *
 * ```kotlin
 * withWorkspaceContext(workspaceId) {
 *     val resumes = resumeRepository.findAll()
 *     // RLS filters results to only this workspace
 * }
 * ```
 *
 * @param workspaceId The workspace ID to set in context
 * @param block The suspend block to execute with workspace context
 * @return The result of the block
 */
suspend inline fun <T> withWorkspaceContext(
    workspaceId: UUID,
    crossinline block: suspend () -> T
): T {
    val workspaceContext = WorkspaceCoroutineContext(workspaceId)
    val reactorContext = coroutineContext[ReactorContext]?.context ?: Context.empty()
    val enrichedReactorContext = workspaceContext.toReactorContextModifier()(reactorContext)

    return kotlinx.coroutines.withContext(
        workspaceContext + ReactorContext(enrichedReactorContext),
    ) {
        block()
    }
}

/**
 * Gets the current workspace ID from either coroutine or reactor context.
 *
 * Checks in order:
 * 1. Coroutine context (WorkspaceCoroutineContext)
 * 2. Reactor context (via ReactorContext in coroutine context)
 *
 * @return The workspace UUID if found, null otherwise
 */
suspend fun currentWorkspaceId(): UUID? {
    // First check coroutine context
    coroutineContext[WorkspaceCoroutineContext]?.let { return it.workspaceId }

    // Then check Reactor context (if running in reactive context)
    coroutineContext[ReactorContext]?.context?.let { reactorContext ->
        return WorkspaceContextHolder.getFromContext(reactorContext)
    }

    return null
}
