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
     * Finds all waitlist entries by source.
     *
     * @param source The source to filter by.
     * @return A flow of matching waitlist entries.
     */
    fun findBySource(source: String): Flow<WaitlistEntryEntity>

    /**
     * Counts waitlist entries by source.
     *
     * @param source The source to count.
     * @return The number of entries from that source.
     */
    @Query("SELECT COUNT(*) FROM waitlist WHERE source = :source")
    suspend fun countBySource(source: String): Long
}
