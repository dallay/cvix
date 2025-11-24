package com.loomify.resume.application.get

import com.loomify.common.domain.Service
import com.loomify.resume.domain.ResumeDocument
import com.loomify.resume.domain.ResumeRepository
import com.loomify.resume.domain.exception.ResumeNotFoundException
import java.util.*
import org.slf4j.LoggerFactory

/**
 *
 * @created 20/11/25
 */
@Service
class ResumeRetrieval(
    private val resumeRepository: ResumeRepository,
) {
    /**
     * Retrieves a resume document by ID for a specific user.
     *
     * @param id The resume document ID
     * @param userId The authenticated user ID
     * @return The resume document if found
     * @throws ResumeNotFoundException if no resume exists for the given id/userId
     */
    suspend fun retrieve(
        id: UUID,
        userId: UUID,
    ): ResumeDocument {
        log.debug("Retrieving resume - id={}, userId={}", id, userId)

        return resumeRepository.findById(id, userId)
            ?: throw ResumeNotFoundException("Resume not found: $id")
    }

    companion object {
        private val log = LoggerFactory.getLogger(ResumeRetrieval::class.java)
    }
}
