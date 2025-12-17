package com.cvix.waitlist.infrastructure.persistence.repository

import com.cvix.waitlist.infrastructure.persistence.entity.WaitlistEntryEntity
import java.util.*
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

/**
 * R2DBC repository interface for waitlist entries.
 *
 * This repository provides CRUD operations and custom queries
 * for the waitlist table.
 */
@Repository
interface WaitlistR2dbcRepository : CoroutineCrudRepository<WaitlistEntryEntity, UUID> {

    /**
     * Finds a waitlist entry by email.
     *
     * @param email The email to search for.
     * @return The waitlist entry if found, null otherwise.
     */
    suspend fun findByEmail(email: String): WaitlistEntryEntity?

    /**
     * Checks if a waitlist entry exists for the given email.
     *
     * @param email The email to check.
     * @return True if exists, false otherwise.
     */
    suspend fun existsByEmail(email: String): Boolean

    /**
     * Finds all waitlist entries by raw source.
     *
     * @param sourceRaw The raw source to filter by.
     * @return A flow of matching waitlist entries.
     */
    fun findBySourceRaw(sourceRaw: String): Flow<WaitlistEntryEntity>

    /**
     * Finds all waitlist entries by normalized source.
     *
     * @param sourceNormalized The normalized source to filter by.
     * @return A flow of matching waitlist entries.
     */
    fun findBySourceNormalized(sourceNormalized: String): Flow<WaitlistEntryEntity>

    /**
     * Counts waitlist entries by raw source.
     *
     * @param sourceRaw The raw source to count.
     * @return The number of entries from that raw source.
     */
    @Query("SELECT COUNT(*) FROM waitlist WHERE source_raw = :sourceRaw")
    suspend fun countBySourceRaw(sourceRaw: String): Long

    /**
     * Counts waitlist entries by normalized source.
     *
     * @param sourceNormalized The normalized source to count.
     * @return The number of entries from that normalized source.
     */
    @Query("SELECT COUNT(*) FROM waitlist WHERE source_normalized = :sourceNormalized")
    suspend fun countBySourceNormalized(sourceNormalized: String): Long
}
