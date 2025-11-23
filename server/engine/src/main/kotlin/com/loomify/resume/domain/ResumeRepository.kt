package com.loomify.resume.domain

import java.util.UUID

/**
 * Repository port for Resume aggregate persistence.
 * Defines the contract for resume data access following Hexagonal Architecture.
 * Uses coroutines for reactive, non-blocking operations.
 *
 * Works with ResumeDocument (aggregate root with metadata) rather than pure Resume.
 */
interface ResumeRepository {
    /**
     * Saves a resume document (create or update).
     * @param document The resume document to save
     * @return The saved resume document
     */
    suspend fun save(document: ResumeDocument): ResumeDocument

    /**
     * Finds a resume document by ID.
     * @param id The resume ID
     * @param userId The authenticated user ID (for authorization)
     * @return The resume document if found and authorized, null otherwise
     */
    suspend fun findById(id: UUID, userId: UUID): ResumeDocument?

    /**
     * Finds all resume documents for a user in a workspace.
     * @param userId The authenticated user ID
     * @param workspaceId The workspace ID
     * @return All resume documents for the user in the workspace
     */
    suspend fun findByUserIdAndWorkspaceId(userId: UUID, workspaceId: UUID): List<ResumeDocument>

    /**
     * Deletes a resume document by ID.
     * @param id The resume ID
     * @param userId The authenticated user ID (for authorization)
     */
    suspend fun deleteById(id: UUID, userId: UUID)

    /**
     * Deletes a resume document if it exists and the user is authorized.
     * @param id The resume ID
     * @param userId The authenticated user ID (for authorization)
     * @return The number of rows affected (0 if not found or unauthorized)
     */
    suspend fun deleteIfAuthorized(id: UUID, userId: UUID): Long

    /**
     * Checks if a resume document exists.
     * @param id The resume ID
     * @param userId The authenticated user ID (for authorization)
     * @return true if exists and authorized
     */
    suspend fun existsById(id: UUID, userId: UUID): Boolean
}
