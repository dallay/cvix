package com.loomify.resume.infrastructure.persistence

import com.loomify.resume.domain.ResumeDocument
import com.loomify.resume.domain.ResumeRepository
import com.loomify.resume.infrastructure.persistence.mapper.ResumeMapper.toDomain
import com.loomify.resume.infrastructure.persistence.mapper.ResumeMapper.toEntity
import com.loomify.resume.infrastructure.persistence.repository.ResumeReactiveR2dbcRepository
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

    override suspend fun save(document: ResumeDocument): ResumeDocument {
        log.debug("Saving resume document id: {}, userId: {}", document.id, document.userId)

        val entity = document.toEntity()
        val saved = resumeReactiveR2dbcRepository.save(entity)

        log.debug("Resume document saved with id: {}", saved.id)
        return saved.toDomain()
    }

    override suspend fun findById(id: UUID, userId: UUID): ResumeDocument? {
        log.debug("Finding resume by id: {}, userId: {}", id, userId)
        return resumeReactiveR2dbcRepository.findByIdAndUserId(id, userId)?.toDomain()
    }

    override suspend fun findByUserIdAndWorkspaceId(userId: UUID, workspaceId: UUID): List<ResumeDocument> {
        log.debug("Finding all resumes for userId: {}, workspaceId: {}", userId, workspaceId)
        return resumeReactiveR2dbcRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
            .map { it.toDomain() }
    }

    override suspend fun deleteById(id: UUID, userId: UUID) {
        log.debug("Deleting resume by id: {}, userId: {}", id, userId)
        resumeReactiveR2dbcRepository.deleteByIdAndUserId(id, userId)
    }

    override suspend fun existsById(id: UUID, userId: UUID): Boolean {
        log.debug("Checking if resume exists by id: {}, userId: {}", id, userId)
        return resumeReactiveR2dbcRepository.existsByIdAndUserId(id, userId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ResumeR2dbcRepository::class.java)
    }
}
