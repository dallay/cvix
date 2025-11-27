package com.loomify.resume.application.template

import com.loomify.common.domain.Service
import com.loomify.common.domain.bus.query.QueryHandler
import com.loomify.resume.application.TemplateMetadataResponses
import org.slf4j.LoggerFactory

@Service
class ListTemplatesQueryHandler(
    private val templateCatalog: TemplateCatalog
) : QueryHandler<ListTemplatesQuery, TemplateMetadataResponses> {

    override suspend fun handle(query: ListTemplatesQuery): TemplateMetadataResponses {
        log.debug("Listing templates limit={}", query.limit)
        val listTemplates = templateCatalog.listTemplates(limit = query.limit)
        return TemplateMetadataResponses.from(listTemplates)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ListTemplatesQueryHandler::class.java)
    }
}
