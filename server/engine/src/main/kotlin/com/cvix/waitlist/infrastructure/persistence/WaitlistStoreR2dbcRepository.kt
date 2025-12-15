package com.cvix.waitlist.infrastructure.persistence

import com.cvix.common.domain.vo.email.Email
import com.cvix.waitlist.domain.WaitlistEntry
import com.cvix.waitlist.domain.WaitlistRepository
import com.cvix.waitlist.infrastructure.persistence.mapper.WaitlistEntryMapper.toDomain
import com.cvix.waitlist.infrastructure.persistence.mapper.WaitlistEntryMapper.toEntity
import com.cvix.waitlist.infrastructure.persistence.repository.WaitlistR2dbcRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

/**
 * R2DBC implementation of the WaitlistRepository.
 *
 * This adapter implements the domain repository interface using Spring Data R2DBC.
 *
 * @property waitlistR2dbcRepository The Spring Data R2DBC repository.
 */
@Repository
class WaitlistStoreR2dbcRepository(
    private val waitlistR2dbcRepository: WaitlistR2dbcRepository,
) : WaitlistRepository {

    /**
     * Saves a new waitlist entry.
     *
     * @param entry The waitlist entry to save.
     * @return The saved entry.
     */
    override suspend fun save(entry: WaitlistEntry): WaitlistEntry {
        logger.debug("Saving waitlist entry with ID: {}", entry.id.id)
        return try {
            val entity = entry.toEntity()
            val savedEntity = waitlistR2dbcRepository.save(entity)
            logger.info("Successfully saved waitlist entry with ID: {}", entry.id.id)
            savedEntity.toDomain()
        } catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
            logger.error("Failed to save waitlist entry with ID: {}", entry.id.id, error)
            throw error
        }
    }

    /**
     * Finds a waitlist entry by email.
     *
     * @param email The email to search for.
     * @return The entry if found, or null if not found.
     */
    override suspend fun findByEmail(email: Email): WaitlistEntry? {
        logger.debug("Finding waitlist entry by email")
        val emailValue = email.value
        val entity = waitlistR2dbcRepository.findByEmail(emailValue)
        return entity?.toDomain()
    }

    /**
     * Checks if an email already exists in the waitlist.
     *
     * @param email The email to check.
     * @return true if exists, false otherwise.
     */
    override suspend fun existsByEmail(email: Email): Boolean {
        logger.debug("Checking if email exists in waitlist")
        val emailValue = email.value
        return waitlistR2dbcRepository.existsByEmail(emailValue)
    }

    /**
     * Counts the total number of entries in the waitlist.
     *
     * @return The count.
     */
    override suspend fun count(): Long {
        logger.debug("Counting total waitlist entries")
        return waitlistR2dbcRepository.count()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WaitlistStoreR2dbcRepository::class.java)
    }
}
