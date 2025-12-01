package com.cvix.resume.application.template

import com.cvix.common.domain.bus.query.Query
import com.cvix.resume.application.TemplateMetadataResponses

/**
 * Query to retrieve all available resume templates.
 */
data class ListTemplatesQuery(
    val limit: Int = 20
) : Query<TemplateMetadataResponses>
