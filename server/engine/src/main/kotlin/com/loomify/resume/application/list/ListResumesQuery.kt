package com.loomify.resume.application.list

import com.loomify.common.domain.bus.query.Query
import com.loomify.resume.application.ResumeDocumentResponses
import java.util.UUID

private const val MAX_ITEMS = 100

/**
 * Query to list resume documents for a user in a workspace.
 * Part of the CQRS pattern in the application layer.
 *
 * Supports cursor-based pagination for efficient large result sets.
 *
 * @property userId The authenticated user
 * @property workspaceId The workspace to list resumes from
 * @property limit Maximum number of results to return (default: 50, max: 100)
 * @property cursor Cursor for pagination (UUID of last resume from previous page)
 */
data class ListResumesQuery(
    val userId: UUID,
    val workspaceId: UUID,
    val limit: Int = 50,
    val cursor: UUID? = null,
) : Query<ResumeDocumentResponses> {
    init {
        require(limit in 1..MAX_ITEMS) {
            "Limit must be between 1 and 100, got $limit"
        }
    }
}
