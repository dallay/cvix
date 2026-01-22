package com.cvix.subscriber.infrastructure.persistence.repository

import com.cvix.spring.boot.repository.ReactiveSearchRepository
import com.cvix.subscriber.domain.SubscriberStatus
import com.cvix.subscriber.infrastructure.persistence.entity.SubscriberEntity
import java.util.UUID
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = true)
interface SubscriberReactiveR2dbcRepository :
    CoroutineCrudRepository<SubscriberEntity, UUID>,
    ReactiveSearchRepository<SubscriberEntity> {

    /**
     * Finds all subscribers by their status.
     *
     * @param status The status to filter by.
     * @return A list of matching subscriber entities.
     */
    suspend fun findAllByStatus(status: SubscriberStatus): List<SubscriberEntity>

    /**
     * Finds a subscriber by their email and source.
     *
     * @param email The email address.
     * @param source The subscription source.
     * @return The subscriber entity if found, null otherwise.
     */
    suspend fun findByEmailAndSource(email: String, source: String): SubscriberEntity?

    /**
     * Checks if a subscriber exists with the given email and source.
     *
     * @param email The email address.
     * @param source The subscription source.
     * @return True if it exists, false otherwise.
     */
    suspend fun existsByEmailAndSource(email: String, source: String): Boolean

    /**
     * Finds all subscribers matching a specific metadata key-value pair.
     * Uses PostgreSQL JSONB path operator to search within the attributes column.
     *
     * @param key The metadata key.
     * @param value The metadata value.
     * @return A list of matching subscriber entities.
     */
    @Query("SELECT * FROM subscribers WHERE attributes->'metadata'->>:key = :value")
    suspend fun findAllByMetadata(key: String, value: String): List<SubscriberEntity>
}
