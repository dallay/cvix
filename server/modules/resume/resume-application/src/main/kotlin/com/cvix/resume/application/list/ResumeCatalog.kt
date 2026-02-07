package com.cvix.resume.application.list

import com.cvix.common.domain.Service
import com.cvix.resume.domain.ResumeDocument
import com.cvix.resume.domain.ResumeRepository
import java.util.UUID
import org.slf4j.LoggerFactory

/**
 * Catalog service for managing resume documents.
 * @created 20/11/25
 */
@Service
class ResumeCatalog(
    private val resumeRepository: ResumeRepository
) {
    /**
     * Lists resume documents for a user in a workspace with pagination.
     *
     * @param userId The authenticated user ID
     * @param workspaceId The workspace ID to list resumes from
     * @param limit Maximum number of results to return (default: 50, clamped to [0, MAX_LIMIT])
     * @param cursor Cursor for pagination (UUID of last resume from previous page)
     * @return List of resume documents (may be empty)
     */
    suspend fun listResumes(
        userId: UUID,
        workspaceId: UUID,
        limit: Int = 50,
        cursor: UUID? = null,
    ): List<ResumeDocument> {
        log.debug(
            "Resume Catalog Listing resumes for user={}, workspace={}",
            userId,
            workspaceId,
        )
        // For now, return all resumes for the user in the workspace
        // Cursor pagination will be implemented when we add proper sorting and filtering
        // Fetch from persistence (already ordered by updated_at DESC at the repo level)
        val resumes = resumeRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
            // Defensive: eliminate any accidental duplicates by ID while preserving order
            .distinctBy { it.id.value }

        // Normalize and clamp the incoming limit
        val normalizedLimit = limit.coerceIn(0, MAX_LIMIT)

        // Apply limit
        val result = if (cursor != null) {
            // Find position of cursor and take next 'normalizedLimit' items
            val cursorIndex = resumes.indexOfFirst { it.id.value == cursor }
            if (cursorIndex >= 0) {
                resumes.drop(cursorIndex + 1).take(normalizedLimit)
            } else {
                resumes.take(normalizedLimit) // If cursor is unknown, fall back to first page
            }
        } else {
            resumes.take(normalizedLimit)
        }

        log.debug("Found {} resumes", result.size)
        return result
    }

    companion object {
        private val log = LoggerFactory.getLogger(ResumeCatalog::class.java)

        /**
         * Maximum allowed page size for resume listings.
         */
        const val MAX_LIMIT = 100
    }
}
