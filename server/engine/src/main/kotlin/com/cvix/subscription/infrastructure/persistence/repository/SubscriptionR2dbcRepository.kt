package com.cvix.subscription.infrastructure.persistence.repository

import com.cvix.subscription.infrastructure.persistence.entity.SubscriptionEntity
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data R2DBC repository interface for subscriptions.
 *
 * @created 12/11/25
 */
@Repository
interface SubscriptionR2dbcRepository : CoroutineCrudRepository<SubscriptionEntity, UUID> {

    /**
     * Finds the active subscription for a user.
     *
     * @param userId The user identifier
     * @return The active subscription entity if found, null otherwise
     */
    @Query(
        """
        SELECT * FROM subscriptions
        WHERE user_id = :userId
        AND status = 'ACTIVE'
        ORDER BY valid_from DESC
        LIMIT 1
        """,
    )
    suspend fun findActiveByUserId(userId: UUID): SubscriptionEntity?

    /**
     * Finds all subscriptions for a user, ordered by valid_from descending.
     *
     * @param userId The user identifier
     * @return Flow of subscription entities
     */
    @Query(
        """
        SELECT * FROM subscriptions
        WHERE user_id = :userId
        ORDER BY valid_from DESC
        """,
    )
    fun findAllByUserId(userId: UUID): Flow<SubscriptionEntity>

    /**
     * Custom save method with explicit type casting for PostgreSQL ENUM types.
     *
     * This is necessary because R2DBC doesn't automatically handle custom PostgreSQL ENUM types.
     * We need to explicitly cast String values to subscription_tier and subscription_status types.
     *
     * Returns the number of rows affected (nullable to handle test contexts where R2DBC may return null).
     */
    @Query(
        """
        INSERT INTO subscriptions (id, user_id, tier, status, valid_from, valid_until, created_at, updated_at)
        VALUES (:#{#entity.id}, :#{#entity.userId}, :#{#entity.tier}::subscription_tier, :#{#entity.status}::subscription_status,
                :#{#entity.validFrom}, :#{#entity.validUntil}, COALESCE(:#{#entity.createdAt}, CURRENT_TIMESTAMP),
                CURRENT_TIMESTAMP)
        ON CONFLICT (id) DO UPDATE SET
            tier = :#{#entity.tier}::subscription_tier,
            status = :#{#entity.status}::subscription_status,
            valid_from = :#{#entity.validFrom},
            valid_until = :#{#entity.validUntil},
            updated_at = CURRENT_TIMESTAMP
        """,
    )
    suspend fun saveWithTypecast(entity: SubscriptionEntity): Int?

    /**
     * Deletes all subscriptions for a user (test utility).
     *
     * Returns the number of rows deleted (nullable to handle test contexts where R2DBC may return null).
     */
    @Query("DELETE FROM subscriptions WHERE user_id = :userId")
    suspend fun deleteAllByUserId(userId: UUID): Int?
}
