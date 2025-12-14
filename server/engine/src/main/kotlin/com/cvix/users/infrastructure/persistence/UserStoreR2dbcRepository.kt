package com.cvix.users.infrastructure.persistence

import com.cvix.users.infrastructure.persistence.repository.UserR2dbcRepository
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository

/**
 * Adapter to persist rows into the local users table.
 */
@Repository
class UserStoreR2dbcRepository(
    private val userR2dbcRepository: UserR2dbcRepository
) {
    suspend fun create(id: UUID, email: String, fullName: String) {
        try {
            userR2dbcRepository.insertIgnoreConflict(id, email, fullName)
        } catch (e: DuplicateKeyException) {
            log.debug("User {} already exists in local DB, ignoring", id, e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserStoreR2dbcRepository::class.java)
    }
}
