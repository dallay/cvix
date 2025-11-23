package com.loomify.resume.application.list

import com.loomify.common.domain.Service
import com.loomify.common.domain.bus.query.QueryHandler
import com.loomify.engine.workspace.application.security.WorkspaceAuthorizationService
import com.loomify.resume.application.ResumeDocumentResponses
import org.slf4j.LoggerFactory

/**
 * Query handler for listing resume documents.
 * Supports cursor-based pagination.
 */
@Service
class ListResumesQueryHandler(
    private val resumeCatalog: ResumeCatalog,
    private val workspaceAuthorizationService: WorkspaceAuthorizationService,
) : QueryHandler<ListResumesQuery, ResumeDocumentResponses> {
    /**
     * Handles the list resumes query.
     * @param query The query containing user ID, workspace ID, and pagination params
     * @return List of resume documents (may be empty)
     */
    override suspend fun handle(query: ListResumesQuery): ResumeDocumentResponses {
        log.debug(
            "Listing resumes - userId={}, workspaceId={}, limit={}, cursor={}",
            query.userId,
            query.workspaceId,
            query.limit,
            query.cursor,
        )
        // Authorization check: ensure user is a member of the workspace
        workspaceAuthorizationService.ensureAccess(query.workspaceId, query.userId)
        val resumeDocuments = resumeCatalog.listResumes(
            userId = query.userId,
            workspaceId = query.workspaceId,
            limit = query.limit,
            cursor = query.cursor,
        )
        return ResumeDocumentResponses.from(resumeDocuments)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ListResumesQueryHandler::class.java)
    }
}
