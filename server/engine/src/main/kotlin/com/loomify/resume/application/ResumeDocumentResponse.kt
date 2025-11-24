package com.loomify.resume.application

import com.loomify.common.domain.bus.query.Response
import com.loomify.resume.domain.Resume
import com.loomify.resume.domain.ResumeDocument
import java.time.Instant
import java.util.UUID

/**
 * Represents a response containing details of a single resume document.
 *
 * @property id The unique identifier of the resume document.
 * @property userId The unique identifier of the user who owns the resume document.
 * @property workspaceId The unique identifier of the workspace associated with the resume document.
 * @property title The title of the resume document.
 * @property content The content of the resume document, represented as a `Resume` object.
 * @property createdAt The timestamp when the resume document was created.
 * @property updatedAt The timestamp when the resume document was last updated, or null if never updated.
 * @property createdBy The identifier of the user who created the resume document.
 * @property updatedBy The identifier of the user who last updated the resume document, or null if never updated.
 */
data class ResumeDocumentResponse(
    val id: UUID,
    val userId: UUID,
    val workspaceId: UUID,
    val title: String,
    val content: Resume,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val createdBy: String,
    val updatedBy: String?,
) : Response {
    companion object {
        /**
         * Converts a `ResumeDocument` object into a `ResumeDocumentResponse`.
         *
         * @param resumeDocument The `ResumeDocument` to convert.
         * @return A `ResumeDocumentResponse` containing the details of the given `ResumeDocument`.
         */
        fun from(resumeDocument: ResumeDocument) = ResumeDocumentResponse(
            id = resumeDocument.id.id,
            userId = resumeDocument.userId,
            workspaceId = resumeDocument.workspaceId,
            title = resumeDocument.title,
            content = resumeDocument.content,
            createdAt = resumeDocument.createdAt,
            updatedAt = resumeDocument.updatedAt,
            createdBy = resumeDocument.createdBy,
            updatedBy = resumeDocument.updatedBy,
        )
    }
}

/**
 * Represents a collection of resume document responses.
 *
 * @property data A list of `ResumeDocumentResponse` objects.
 */
data class ResumeDocumentResponses(val data: List<ResumeDocumentResponse>) : Response {
    companion object {
        /**
         * Converts a list of `ResumeDocument` objects into a `ResumeDocumentResponses`.
         *
         * @param resumeDocuments The list of `ResumeDocument` objects to convert.
         * @return A `ResumeDocumentResponses` containing the converted list of `ResumeDocumentResponse` objects.
         */
        fun from(resumeDocuments: List<ResumeDocument>) = ResumeDocumentResponses(
            data = resumeDocuments.map { ResumeDocumentResponse.from(it) },
        )
    }
}
