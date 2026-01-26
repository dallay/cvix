package com.cvix.form.application.search

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.query.QueryHandler
import com.cvix.common.domain.presentation.pagination.CursorPageResponse
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.form.application.SubscriberFormResponse
import org.slf4j.LoggerFactory

/**
 * Handles the search for subscriber forms.
 */
@Service
class SearchSubscriberFormsQueryHandler(
    private val workspaceAuthorization: WorkspaceAuthorization,
    private val searcher: SubscriberFormsSearcher,
) : QueryHandler<SearchSubscriberFormsQuery, CursorPageResponse<SubscriberFormResponse>> {

    override suspend fun handle(query: SearchSubscriberFormsQuery): CursorPageResponse<SubscriberFormResponse> {
        log.debug("Searching subscriber forms for workspace: {}", query.workspaceId)
        workspaceAuthorization.ensureAccess(query.workspaceId, query.userId)

        return searcher.search(
            criteria = query.criteria,
            size = query.size,
            cursor = query.cursor,
            sort = query.sort,
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(SearchSubscriberFormsQueryHandler::class.java)
    }
}
