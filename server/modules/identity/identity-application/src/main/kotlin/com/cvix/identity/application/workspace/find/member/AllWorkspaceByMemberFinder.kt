package com.cvix.identity.application.workspace.find.member

import com.cvix.common.domain.Service
import com.cvix.identity.application.workspace.WorkspaceResponses
import com.cvix.identity.domain.user.UserId
import com.cvix.identity.domain.workspace.WorkspaceFinderRepository
import java.util.UUID
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
