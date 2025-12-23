package com.cvix.config.db

import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

private val log: Logger = LoggerFactory.getLogger(WorkspaceContextWebFilter::class.java)

/**
 * WebFilter that extracts the workspace ID from the request and propagates it
 * through the reactive context for Row-Level Security (RLS) enforcement.
 *
 * ## Workspace ID Sources (in order of precedence)
 *
 * 1. **HTTP Header**: `X-Workspace-Id` header (preferred for API clients)
 * 2. **Query Parameter**: `workspaceId` query parameter
 *
 * If neither is present, the request proceeds without workspace context.
 * This allows public endpoints and endpoints that get workspaceId from request
 * body to work normally.
 *
 * ## Usage
 *
 * ### Option 1: Client sends header (recommended for API clients)
 * ```http
 * GET /api/resumes HTTP/1.1
 * X-Workspace-Id: 550e8400-e29b-41d4-a716-446655440000
 * Authorization: Bearer <token>
 * ```
 *
 * ### Option 2: Query parameter
 * ```http
 * GET /api/resumes?workspaceId=550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
 * Authorization: Bearer <token>
 * ```
 *
 * ### Option 3: Programmatic (for endpoints with workspaceId in body)
 * When the workspace ID comes from the request body, services should
 * use [WorkspaceContextHolder.withWorkspace] to set the context:
 *
 * ```kotlin
 * suspend fun createResume(command: CreateResumeCommand) {
 *     // After validating workspace access
 *     workspaceAuthorizationService.ensureAccess(command.workspaceId, command.userId)
 *
 *     // Propagate workspace context for RLS
 *     return resumeRepository.save(resume)
 *         .contextWrite(WorkspaceContextHolder.withWorkspace(command.workspaceId))
 * }
 * ```
 *
 * ## RLS Flow
 *
 * ```
 * Request → WorkspaceContextWebFilter → Reactive Context
 *                                              ↓
 * DatabaseConfig → WorkspaceConnectionFactoryDecorator
 *                                              ↓
 *                              SET LOCAL cvix.current_workspace = '<id>'
 *                                              ↓
 *                              PostgreSQL RLS policies filter data
 * ```
 *
 * @see WorkspaceContextHolder
 * @see WorkspaceConnectionFactoryDecorator
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // Run early, after security filters
class WorkspaceContextWebFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val workspaceId = extractWorkspaceId(exchange)

        return if (workspaceId != null) {
            if (log.isDebugEnabled) {
                log.debug("Workspace context set from request: {}", workspaceId)
            }
            chain.filter(exchange)
                .contextWrite(WorkspaceContextHolder.withWorkspace(workspaceId))
        } else {
            // No workspace in request - proceed without RLS context
            // This is normal for:
            // - Public endpoints
            // - Endpoints where workspaceId comes from request body
            // - Authentication endpoints
            chain.filter(exchange)
        }
    }

    /**
     * Extracts workspace ID from the request.
     *
     * @param exchange The server web exchange
     * @return The workspace UUID if found and valid, null otherwise
     */
    private fun extractWorkspaceId(exchange: ServerWebExchange): UUID? {
        // Try header first
        val headerValue = exchange.request.headers.getFirst(WORKSPACE_HEADER)
        if (!headerValue.isNullOrBlank()) {
            return parseUuid(headerValue, "header")
        }

        // Try query parameter
        val queryValue = exchange.request.queryParams.getFirst(WORKSPACE_QUERY_PARAM)
        if (!queryValue.isNullOrBlank()) {
            return parseUuid(queryValue, "query parameter")
        }

        return null
    }

    /**
     * Safely parses a UUID string.
     *
     * @param value The string value to parse
     * @param source Description of where the value came from (for logging)
     * @return The parsed UUID or null if invalid
     */
    private fun parseUuid(value: String, source: String): UUID? {
        return try {
            UUID.fromString(value)
        } catch (e: IllegalArgumentException) {
            log.warn("Invalid workspace ID from {}: {} - {}", source, value, e.message)
            null
        }
    }
    companion object {
        /**
         * HTTP header name for workspace ID.
         */
        const val WORKSPACE_HEADER = "X-Workspace-Id"

        /**
         * Query parameter name for workspace ID.
         */
        const val WORKSPACE_QUERY_PARAM = "workspaceId"
    }
}
