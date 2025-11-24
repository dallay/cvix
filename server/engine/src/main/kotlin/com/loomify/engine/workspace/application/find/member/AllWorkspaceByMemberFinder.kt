package com.loomify.engine.workspace.application.find.member

import com.loomify.common.domain.Service
import com.loomify.engine.users.domain.UserId
import com.loomify.engine.workspace.application.WorkspaceResponses
import com.loomify.engine.workspace.domain.WorkspaceFinderRepository
import java.util.*
import org.slf4j.LoggerFactory

/**
 * This service is responsible for finding all workspaces.
 *
 * @property finder The repository used to find all workspaces.
 */
@Service
class AllWorkspaceByMemberFinder(private val finder: WorkspaceFinderRepository) {

    /**
     * Finds all workspaces.
     * @param userId The unique identifier of the user.
     *
     * @throws Exception If an error occurs while finding all workspaces.
     * @return The [WorkspaceResponses] containing all workspaces.
     */
    suspend fun findAll(userId: UUID): WorkspaceResponses {
        log.debug("Finding all workspaces for user with id: {}", userId)
        try {
            val workspaces = finder.findByMemberId(UserId(userId))
            return WorkspaceResponses.from(workspaces)
        } catch (exception: Exception) {
            log.error("Failed to find workspaces for user: $userId", exception)
            throw exception
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AllWorkspaceByMemberFinder::class.java)
    }
}
