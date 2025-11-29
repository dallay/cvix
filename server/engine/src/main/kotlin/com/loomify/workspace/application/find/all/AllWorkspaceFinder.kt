package com.loomify.workspace.application.find.all

import com.loomify.common.domain.Service
import com.loomify.workspace.application.WorkspaceResponses
import com.loomify.workspace.domain.WorkspaceFinderRepository
import org.slf4j.LoggerFactory

/**
 * This service is responsible for finding all workspaces.
 *
 * @property finder The repository used to find all workspaces.
 */
@Service
class AllWorkspaceFinder(private val finder: WorkspaceFinderRepository) {

    /**
     * Finds all workspaces.
     *
     * @throws Exception If an error occurs while finding all workspaces.
     * @return The [WorkspaceResponses] containing all workspaces.
     */
    suspend fun findAll(): WorkspaceResponses {
        log.debug("Finding all workspaces")
        val workspaces = finder.findAll().toList()
        return WorkspaceResponses.from(workspaces)
    }

    companion object {
        private val log = LoggerFactory.getLogger(AllWorkspaceFinder::class.java)
    }
}
