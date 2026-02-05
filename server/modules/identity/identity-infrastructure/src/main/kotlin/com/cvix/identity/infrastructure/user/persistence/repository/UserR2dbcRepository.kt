package com.cvix.identity.infrastructure.user.persistence.repository

import com.cvix.identity.infrastructure.user.persistence.entity.UserEntity
import com.cvix.spring.boot.repository.ReactiveSearchRepository
import java.util.UUID
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserR2dbcRepository :
    CoroutineCrudRepository<UserEntity, UUID>,
    ReactiveSearchRepository<UserEntity> {
    @Query(
        """
        INSERT INTO users (id, email, full_name)
        VALUES (:id, :email, :fullName)
        ON CONFLICT (id) DO NOTHING
    """,
    )
    @Modifying
    suspend fun insertIgnoreConflict(id: UUID, email: String, fullName: String)

    /**
     * Find a user by email address
     */
    suspend fun findByEmail(email: String): UserEntity?
}
