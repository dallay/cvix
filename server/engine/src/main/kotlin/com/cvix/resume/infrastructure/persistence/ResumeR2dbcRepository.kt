package com.cvix.resume.infrastructure.persistence

import com.cvix.resume.domain.ResumeDocument
import com.cvix.resume.domain.ResumeRepository
import com.cvix.resume.infrastructure.persistence.mapper.ResumeMapper.toDomain
import com.cvix.resume.infrastructure.persistence.mapper.ResumeMapper.toEntity
import com.cvix.resume.infrastructure.persistence.repository.ResumeReactiveR2dbcRepository
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

/**
 * R2DBC implementation of ResumeRepository using PostgreSQL JSONB.
 *
 * Stores ResumeDocument domain aggregates as JSONB for flexible schema evolution.
 * Uses Spring Data's CoroutineCrudRepository for reactive, non-blocking operations.
 *
 * Architecture flow:
 * - Domain: ResumeDocument (aggregate with metadata + Resume content)
 * - Infrastructure: ResumeEntity (Spring Data entity with JSONB column)
 */
@Repository
class ResumeR2dbcRepository(
    private val resumeReactiveR2dbcRepository: ResumeReactiveR2dbcRepository,
) : ResumeRepository {
    /**
     * Checks if a resume exists by its ID (regardless of user).
     * @param id The resume ID
     * @return true if the resume exists, false otherwise
     */
    override suspend fun existsById(id: UUID): Boolean =
        resumeReactiveR2dbcRepository.existsById(id)

    /**
     * Checks if a resume exists by its ID for a specific user (authorization enforced).
     * @param id The resume ID
     * @param userId The authenticated user ID
     * @return true if the resume exists and is accessible by the user, false otherwise
     */
    override suspend fun existsByIdForUser(id: UUID, userId: UUID): Boolean =
        resumeReactiveR2dbcRepository.existsByIdAndUserId(id, userId)

    /**
     * Saves a resume document (create or update).
     * @param document The resume document to save
     * @return The saved resume document
     */
    override suspend fun save(document: ResumeDocument): ResumeDocument {
        log.debug("Saving resume document id: {}, userId: {}", document.id, document.userId)

        val entity = document.toEntity()
        val saved = resumeReactiveR2dbcRepository.save(entity)

        log.debug("Resume document saved with id: {}", saved.id)
        return document.copy(
            createdAt = saved.createdAt,
            updatedAt = saved.updatedAt ?: saved.createdAt,
        )
    }

    /**
     * Finds a resume document by ID.
     * @param id The resume ID
     * @param userId The authenticated user ID (for authorization)
     * @return The resume document if found and authorized, null otherwise
     */
    override suspend fun findById(id: UUID, userId: UUID): ResumeDocument? {
        log.debug("Finding resume by id: {}, userId: {}", id, userId)
        return resumeReactiveR2dbcRepository.findByIdAndUserId(id, userId)?.toDomain()
    }

    /**
     * Finds all resume documents for a user in a workspace.
     * @param userId The authenticated user ID
     * @param workspaceId The workspace ID
     * @return All resume documents for the user in the workspace
     */
    override suspend fun findByUserIdAndWorkspaceId(
        userId: UUID,
        workspaceId: UUID
    ): List<ResumeDocument> {
        log.debug("Finding all resumes for userId: {}, workspaceId: {}", userId, workspaceId)
        return resumeReactiveR2dbcRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
            .map { it.toDomain() }
    }

    /**
     * Deletes a resume document by ID for a specific user (authorization enforced).
     * @param id The resume ID
     * @param userId The authenticated user ID
     * @return The number of rows affected (0 if not found or unauthorized)
     */
    override suspend fun deleteByIdForUser(id: UUID, userId: UUID) =
        resumeReactiveR2dbcRepository.deleteByIdAndUserId(id, userId)

    /**
     *  Deletes a resume document if it exists and the user is authorized.
     * @param id The resume ID
     * @param userId The authenticated user ID (for authorization)
     * @return The number of rows affected (0 if not found or unauthorized)
     */
    override suspend fun deleteIfAuthorized(id: UUID, userId: UUID): Long =
        resumeReactiveR2dbcRepository.deleteByIdAndUserId(id, userId)

    companion object {
        private val log = LoggerFactory.getLogger(ResumeR2dbcRepository::class.java)
    }
}
