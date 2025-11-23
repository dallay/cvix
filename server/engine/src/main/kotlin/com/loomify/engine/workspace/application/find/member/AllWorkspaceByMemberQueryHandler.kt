package com.loomify.engine.workspace.application.find.member

import com.loomify.common.domain.Service
import com.loomify.common.domain.bus.query.QueryHandler
import com.loomify.engine.workspace.application.WorkspaceResponses
import org.slf4j.LoggerFactory

/**
 * This class represents a query to find all workspaces.
 * @property finder The [AllWorkspaceByMemberFinder] to find all workspaces.
 */
@Service
class AllWorkspaceByMemberQueryHandler(
    private val finder: AllWorkspaceByMemberFinder
) : QueryHandler<AllWorkspaceByMemberQuery, WorkspaceResponses> {
    /**
     * Handles a query
     *
     * @param query the query to handle
     * @return the response to the query handled
     */
    override suspend fun handle(query: AllWorkspaceByMemberQuery): WorkspaceResponses {
        log.debug("Handling query: {}", query)
        return finder.findAll(query.userId.toString())
    }

    companion object {
        private val log = LoggerFactory.getLogger(AllWorkspaceByMemberQueryHandler::class.java)
    }
}
