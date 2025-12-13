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

    override suspend fun existsByIdForUser(id: UUID, userId: UUID): Boolean =
        resumeReactiveR2dbcRepository.existsByIdAndUserId(id, userId)

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

    override suspend fun findById(id: UUID, userId: UUID): ResumeDocument? {
        log.debug("Finding resume by id: {}, userId: {}", id, userId)
        return resumeReactiveR2dbcRepository.findByIdAndUserId(id, userId)?.toDomain()
    }

    override suspend fun findByUserIdAndWorkspaceId(
        userId: UUID,
        workspaceId: UUID
    ): List<ResumeDocument> {
        log.debug("Finding all resumes for userId: {}, workspaceId: {}", userId, workspaceId)
        return resumeReactiveR2dbcRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
            .map { it.toDomain() }
    }

    override suspend fun deleteByIdForUser(id: UUID, userId: UUID) {
        resumeReactiveR2dbcRepository.deleteByIdAndUserId(id, userId)
    }

    override suspend fun deleteIfAuthorized(id: UUID, userId: UUID): Long =
        resumeReactiveR2dbcRepository.deleteByIdAndUserId(id, userId)

    companion object {
        private val log = LoggerFactory.getLogger(ResumeR2dbcRepository::class.java)
    }
}
