package com.loomify.resume.application.template

import com.loomify.common.domain.bus.query.Query
import com.loomify.resume.application.TemplateMetadataResponses

/**
 * Query to retrieve all available resume templates.
 */
data class ListTemplatesQuery(
    val limit: Int = 20
) : Query<TemplateMetadataResponses>
