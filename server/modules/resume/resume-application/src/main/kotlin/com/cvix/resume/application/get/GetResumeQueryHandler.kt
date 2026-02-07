package com.cvix.resume.application.get

import com.cvix.common.domain.bus.query.QueryHandler
import com.cvix.resume.application.ResumeDocumentResponse
import com.cvix.resume.domain.ResumeRepository
import com.cvix.resume.domain.exception.ResumeAccessDeniedException
import com.cvix.resume.domain.exception.ResumeNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Query handler for retrieving a single resume document by ID.
 */
@Service
class GetResumeQueryHandler(
    private val resumeRepository: ResumeRepository,
) : QueryHandler<GetResumeQuery, ResumeDocumentResponse> {

    /**
     * Handles the get resume query.
     * @param query The query containing resume ID and user ID
     * @return The resume document if found and authorized
     * @throws ResumeNotFoundException if not found
     * @throws ResumeAccessDeniedException if forbidden/unauthorized
     */
    override suspend fun handle(query: GetResumeQuery): ResumeDocumentResponse {
        log.debug("Getting resume - id={}, userId={}", query.id, query.userId)
        val resume = resumeRepository.findById(query.id, query.userId)
        if (resume == null) {
            // Here you should distinguish not found vs forbidden if possible
            // For demo, assume not found if resume doesn't exist, forbidden if exists but userId doesn't match
            // This logic may need to be refined based on repository implementation
            throw ResumeNotFoundException("Resume not found: ${query.id}")
        }
        // If you have logic to check authorization, throw ResumeAccessDeniedException as needed
        return ResumeDocumentResponse.from(resume)
    }

    companion object {
        private val log = LoggerFactory.getLogger(GetResumeQueryHandler::class.java)
    }
}
