package com.cvix.identity.application.workspace.find.all

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.query.QueryHandler
import com.cvix.identity.application.workspace.WorkspaceResponses
import org.slf4j.LoggerFactory

/**
 * This class represents a query to find all workspaces.
 * @property finder The [AllWorkspaceFinder] to find all workspaces.
 */
@Service
class AllWorkspaceQueryHandler(
    private val finder: AllWorkspaceFinder
) : QueryHandler<AllWorkspaceQuery, WorkspaceResponses> {
    /**
     * Handles a query
     *
     * @param query the query to handle
     * @return the response to the query handled
     */
    override suspend fun handle(query: AllWorkspaceQuery): WorkspaceResponses {
        log.debug("Handling query: {}", query)
        return finder.findAll()
    }

    companion object {
        private val log = LoggerFactory.getLogger(AllWorkspaceQueryHandler::class.java)
    }
}
