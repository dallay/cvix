package com.cvix.resume.application.template

import com.cvix.common.domain.bus.query.Query
import com.cvix.resume.application.TemplateMetadataResponses
import java.util.UUID

/**
 * Query to retrieve all available resume templates.
 *
 * @property userId The authenticated user
 * @property workspaceId The workspace context
 * @property limit Maximum number of templates to return (default: 20)
 */
data class ListTemplatesQuery(
    val userId: UUID,
    val workspaceId: UUID,
    val limit: Int = 20
) : Query<TemplateMetadataResponses>
