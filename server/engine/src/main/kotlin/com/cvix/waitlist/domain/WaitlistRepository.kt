package com.cvix.waitlist.domain

import com.cvix.common.domain.vo.email.Email

/**
 * Repository interface for managing waitlist entries.
 *
 * This interface defines the contract for persisting and retrieving
 * waitlist entries from the data store.
 */
interface WaitlistRepository {

    /**
     * Saves a new waitlist entry.
     *
     * @param entry The waitlist entry to save.
     * @return The saved entry.
     */
    suspend fun save(entry: WaitlistEntry): WaitlistEntry

    /**
     * Finds a waitlist entry by email.
     *
     * @param email The email to search for.
     * @return The entry if found, or null if not found.
     */
    suspend fun findByEmail(email: Email): WaitlistEntry?

    /**
     * Checks if an email already exists in the waitlist.
     *
     * @param email The email to check.
     * @return true if exists, false otherwise.
     */
    suspend fun existsByEmail(email: Email): Boolean

    /**
     * Counts the total number of entries in the waitlist.
     *
     * @return The count.
     */
    suspend fun count(): Long
}
