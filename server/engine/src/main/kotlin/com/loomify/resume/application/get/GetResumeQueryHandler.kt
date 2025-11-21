package com.loomify.resume.application.get

import com.loomify.common.domain.bus.query.QueryHandler
import com.loomify.resume.application.ResumeDocumentResponse
import com.loomify.resume.domain.ResumeRepository
import com.loomify.resume.domain.exception.ResumeNotFoundException
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
     * @throws ResumeNotFoundException if not found or unauthorized
     */
    override suspend fun handle(query: GetResumeQuery): ResumeDocumentResponse {
        log.debug("Getting resume - id={}, userId={}", query.id, query.userId)

        return ResumeDocumentResponse.from(
            resumeRepository.findById(query.id, query.userId)
                ?: throw ResumeNotFoundException("Resume not found: ${query.id}"),
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(GetResumeQueryHandler::class.java)
    }
}
