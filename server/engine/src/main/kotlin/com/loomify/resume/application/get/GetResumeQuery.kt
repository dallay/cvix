package com.loomify.resume.application.get

import com.loomify.common.domain.bus.query.Query
import com.loomify.resume.application.ResumeDocumentResponse
import java.util.*

/**
 * Query to retrieve a resume document by ID.
 * Part of the CQRS pattern in the application layer.
 *
 * @property id The resume document ID to retrieve
 * @property userId The authenticated user (for authorization check)
 */
data class GetResumeQuery(
    val id: UUID,
    val userId: UUID,
) : Query<ResumeDocumentResponse>
