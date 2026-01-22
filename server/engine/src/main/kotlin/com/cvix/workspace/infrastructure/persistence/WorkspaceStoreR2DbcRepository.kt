package com.cvix.workspace.infrastructure.persistence

import com.cvix.users.domain.UserId
import com.cvix.workspace.domain.Workspace
import com.cvix.workspace.domain.WorkspaceException
import com.cvix.workspace.domain.WorkspaceFinderRepository
import com.cvix.workspace.domain.WorkspaceId
import com.cvix.workspace.domain.WorkspaceMember
import com.cvix.workspace.domain.WorkspaceMemberRepository
import com.cvix.workspace.domain.WorkspaceRepository
import com.cvix.workspace.domain.WorkspaceRole
import com.cvix.workspace.infrastructure.persistence.mapper.WorkspaceMapper.toDomain
import com.cvix.workspace.infrastructure.persistence.mapper.WorkspaceMapper.toEntity
import com.cvix.workspace.infrastructure.persistence.mapper.toDomain
import com.cvix.workspace.infrastructure.persistence.repository.WorkspaceMemberR2dbcRepository
import com.cvix.workspace.infrastructure.persistence.repository.WorkspaceR2dbcRepository
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.dao.TransientDataAccessResourceException
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class WorkspaceStoreR2DbcRepository(
    private val workspaceRepository: WorkspaceR2dbcRepository,
    private val workspaceMemberRepository: WorkspaceMemberR2dbcRepository
) : WorkspaceRepository,
    WorkspaceFinderRepository,
    WorkspaceMemberRepository {

    /**
     * Create a new workspace.
     *
     * @param workspace The workspace to be created.
     */
    @Transactional("connectionFactoryTransactionManager")
    override suspend fun create(workspace: Workspace) {
        log.debug("Creating workspace with id: {}", workspace.id)
        try {
            // Save workspace
            workspaceRepository.save(workspace.toEntity())
            // Save each member
            workspace.members.forEach { memberId ->
                workspaceMemberRepository.insertWorkspaceMember(
                    workspace.id.value,
                    memberId.value,
                    WorkspaceRole.EDITOR.name,
                )
            }
        } catch (e: DuplicateKeyException) {
            log.error("Error creating workspace with id: {} -> DKE", workspace.id, e)
            throw WorkspaceException("Error creating workspace", e)
        } catch (e: PessimisticLockingFailureException) {
            log.error("Error creating workspace with id: {} -> PLFE", workspace.id, e)
            throw WorkspaceException("Error creating workspace due to a locking issue", e)
        } catch (e: TransientDataAccessResourceException) {
            log.error("Error creating workspace with id: {} -> TDARE", workspace.id, e)
            throw WorkspaceException("Error creating workspace because it already exists", e)
        }
    }

    /**
     * Updates a workspace.
     *
     * @param workspace The workspace to be updated.
     */
    @Transactional("connectionFactoryTransactionManager")
    override suspend fun update(workspace: Workspace) {
        log.debug("Updating workspace with id: {}", workspace.id)
        try {
            // First, check if the workspace exists
            val existingEntity = workspaceRepository.findById(workspace.id.value)
                ?: throw WorkspaceException("Workspace with id ${workspace.id} does not exist")

            // Create entity with proper timestamps for update
            val entityToUpdate = workspace.toEntity().copy(
                createdAt = existingEntity.createdAt,
                createdBy = existingEntity.createdBy,
                updatedAt = Instant.now(),
                updatedBy = "system",
            )

            // Update workspace
            workspaceRepository.save(entityToUpdate)
        } catch (e: DuplicateKeyException) {
            log.error("Error updating workspace with id: {}", workspace.id, e)
            throw WorkspaceException("Error updating workspace", e)
        } catch (e: TransientDataAccessResourceException) {
            log.error("Error updating workspace with id: {}", workspace.id, e)
            throw WorkspaceException("Error updating form because it does not exist", e)
        }
    }

    /**
     * Find a workspace by its unique identifier.
     *
     * @param id The unique identifier of the workspace.
     * @return The workspace with the given unique identifier.
     */
    override suspend fun findById(id: WorkspaceId): Workspace? {
        log.debug("Finding workspace with id: {}", id)
        return workspaceRepository.findById(id.value)?.toDomain()
    }

    /**
     * Finds all workspaces.
     *
     * @return A flow of all workspaces.
     */
    override suspend fun findAll(): List<Workspace> {
        log.debug("Finding all workspaces")
        return workspaceRepository.findAll().toList().map { it.toDomain() }
    }

    /**
     * Finds all workspaces for a user.
     *
     * @param userId The ID of the user.
     * @return A flow of workspaces that the user is a member of.
     */
    override suspend fun findByMemberId(userId: UserId): List<Workspace> {
        log.debug("Finding workspaces by member id: {}", userId)
        return workspaceRepository.findByMemberId(userId.value).toList().map { it.toDomain() }
    }

    /**
     * Finds all workspaces owned by a user.
     *
     * @param userId The ID of the user.
     * @return A flow of workspaces that the user owns.
     */
    override suspend fun findByOwnerId(userId: UserId): List<Workspace> {
        log.debug("Finding workspaces by owner id: {}", userId)
        return workspaceRepository.findByOwnerId(userId.value).toList().map { it.toDomain() }
    }

    /**
     * Deletes a workspace.
     *
     * @param id The workspace id.
     */
    override suspend fun delete(id: WorkspaceId) {
        log.debug("Deleting workspace with id: {}", id)
        workspaceRepository.deleteById(id.value)
    }

    /**
     * Finds all workspace members for a workspace.
     *
     * @param workspaceId The ID of the workspace.
     * @return A flux of workspace member entities.
     */
    override suspend fun findByWorkspaceId(workspaceId: UUID): List<WorkspaceMember> {
        log.debug("Finding workspace members by workspace id: {}", workspaceId)
        return workspaceMemberRepository.findByWorkspaceId(workspaceId).map { it.toDomain() }
    }

    /**
     * Finds all workspaces for a user.
     *
     * @param userId The ID of the user.
     * @return A flux of workspace member entities.
     */
    override suspend fun findByUserId(userId: UUID): List<WorkspaceMember> {
        log.debug("Finding workspace members by user id: {}", userId)
        return workspaceMemberRepository.findByUserId(userId).map { it.toDomain() }
    }

    /**
     * Checks if a user is a member of a workspace.
     *
     * @param workspaceId The ID of the workspace.
     * @param userId The ID of the user.
     * @return True if the user is a member of the workspace, false otherwise.
     */
    override suspend fun existsByWorkspaceIdAndUserId(
        workspaceId: UUID,
        userId: UUID
    ): Boolean {
        log.debug("Checking if user {} is a member of workspace {}", userId, workspaceId)
        return workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)
    }

    /**
     * Inserts a workspace member using a custom query.
     * This is needed due to composite key limitations in Spring Data R2DBC.
     *
     * @param workspaceId The ID of the workspace.
     * @param userId The ID of the user.
     * @param role The role of the user in the workspace.
     * @return The number of affected rows.
     */
    override suspend fun insertWorkspaceMember(
        workspaceId: UUID,
        userId: UUID,
        role: String
    ): Int {
        log.debug("Inserting workspace member with workspaceId: {}, userId: {}, role: {}", workspaceId, userId, role)
        return workspaceMemberRepository.insertWorkspaceMember(workspaceId, userId, role)
    }

    /**
     * Deletes a workspace member using a custom query.
     * This is needed due to composite key limitations in Spring Data R2DBC.
     *
     * @param workspaceId The ID of the workspace.
     * @param userId The ID of the user.
     * @return The number of affected rows.
     */
    override suspend fun deleteByWorkspaceIdAndUserId(
        workspaceId: UUID,
        userId: UUID
    ): Int {
        log.debug("Deleting workspace member with workspaceId: {} and userId: {}", workspaceId, userId)
        return workspaceMemberRepository.deleteByWorkspaceIdAndUserId(workspaceId, userId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(WorkspaceStoreR2DbcRepository::class.java)
    }
}
