package com.cvix.subscription.infrastructure.persistence

import com.cvix.subscription.domain.Subscription
import com.cvix.subscription.domain.SubscriptionException
import com.cvix.subscription.domain.SubscriptionId
import com.cvix.subscription.domain.SubscriptionRepository
import com.cvix.subscription.infrastructure.persistence.mapper.SubscriptionMapper.toDomain
import com.cvix.subscription.infrastructure.persistence.mapper.SubscriptionMapper.toEntity
import com.cvix.subscription.infrastructure.persistence.repository.SubscriptionR2dbcRepository
import java.util.UUID
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository

/**
 * Adapter to persist subscription data using R2DBC.
 *
 * This is the implementation of the SubscriptionRepository port,
 * connecting the domain layer to the PostgreSQL database.
 *
 * Uses CoroutineCrudRepository which handles transactions internally with R2DBC.
 * For explicit transaction management, wrap calls in transactional {} block from the service layer.
 *
 * @created 12/11/25
 */
@Repository
class SubscriptionStoreR2dbcRepository(
    private val subscriptionR2dbcRepository: SubscriptionR2dbcRepository
) : SubscriptionRepository {

    /**
     * Finds a subscription by its unique identifier.
     */
    override suspend fun findById(id: SubscriptionId): Subscription? {
        log.debug("Finding subscription by id: {}", id)
        return subscriptionR2dbcRepository.findById(id.value)?.toDomain()
    }

    /**
     * Finds the active subscription for a given user.
     */
    override suspend fun findActiveByUserId(userId: UUID): Subscription? {
        log.debug("Finding active subscription for user: {}", userId)
        return subscriptionR2dbcRepository.findActiveByUserId(userId)?.toDomain()
    }

    /**
     * Finds all subscriptions for a given user.
     */
    override suspend fun findAllByUserId(userId: UUID): List<Subscription> {
        log.debug("Finding all subscriptions for user: {}", userId)
        return subscriptionR2dbcRepository.findAllByUserId(userId)
            .map { entity -> entity.toDomain() }
            .toList()
    }

    /**
     * Saves a subscription (create or update).
     *
     * Uses custom saveWithTypecast to properly handle PostgreSQL ENUM types.
     *
     * **Null vs 0 Handling:**
     * - `null`: R2DBC driver limitation with custom @Query DML operations. Treated as success with unknown row count.
     * - `0`: Actual rows affected. For UPSERT, this is an error since UPSERT should always affect exactly 1 row.
     *
     * See: https://github.com/spring-projects/spring-data-r2dbc/issues/523
     */
    override suspend fun save(subscription: Subscription) {
        log.debug("Saving subscription: {}", subscription.id)
        try {
            // Capture nullable result directly to distinguish between null (driver limitation)
            // and 0 (actual rows affected, which is an error for UPSERT)
            val rowsAffected = subscriptionR2dbcRepository.saveWithTypecast(subscription.toEntity())

            when (rowsAffected) {
                null ->
                    // R2DBC driver limitation with custom @Query DML operations
                    // Some versions of Spring Data R2DBC return null for custom queries
                    // See: https://github.com/spring-projects/spring-data-r2dbc/issues/523
                    log.warn(
                        "saveWithTypecast returned null for subscription: {}. " +
                            "This indicates R2DBC driver limitation with custom @Query DML operations. " +
                            "Treating as success with unknown row count.",
                        subscription.id,
                    )
                0 -> {
                    // UPSERT (INSERT ... ON CONFLICT DO UPDATE) should always affect exactly 1 row
                    // 0 rows affected indicates a serious query logic error
                    log.error("UPSERT operation affected 0 rows for subscription: {}", subscription.id)
                    throw SubscriptionException("Failed to save subscription: no rows affected")
                }
                else -> log.debug("Subscription saved, rows affected: {}", rowsAffected)
            }
        } catch (e: DuplicateKeyException) {
            log.error("Error saving subscription with id: {} - duplicate key", subscription.id, e)
            throw SubscriptionException("Error saving subscription: duplicate key", e)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            log.error("Error saving subscription with id: {}", subscription.id, e)
            throw SubscriptionException("Error saving subscription", e)
        }
    }

    /**
     * Deletes a subscription.
     *
     * CoroutineCrudRepository.deleteById() is a suspend function that handles transactions internally.
     */
    override suspend fun delete(id: SubscriptionId) {
        log.debug("Deleting subscription: {}", id)
        try {
            subscriptionR2dbcRepository.deleteById(id.value)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            log.error("Error deleting subscription with id: {}", id, e)
            throw SubscriptionException("Error deleting subscription", e)
        }
    }

    /**
     * Deletes all subscriptions for a given user (test utility).
     *
     * **Null vs 0 Handling:**
     * - `null`: R2DBC driver limitation with custom @Query DML operations.
     *   Logged as a warning, treated as 0 rows deleted.
     * - `0`: Valid result indicating no matching subscriptions were found for the user.
     */
    suspend fun deleteAllByUserId(userId: UUID) {
        log.debug("Deleting all subscriptions for user: {}", userId)
        try {
            // Capture nullable result to distinguish null (driver limitation) from 0 (no rows found)
            val rowsDeleted = subscriptionR2dbcRepository.deleteAllByUserId(userId)

            // For DELETE operations, 0 is a legitimate result (no matching rows)
            // but null indicates a driver issue that should be logged
            if (rowsDeleted == null) {
                log.warn(
                    "deleteAllByUserId returned null for user: {}. " +
                        "This indicates R2DBC driver limitation with custom @Query DML operations. " +
                        "Treating as 0 rows deleted.",
                    userId,
                )
            }

            log.debug("Deleted {} subscriptions for user: {}", rowsDeleted ?: 0, userId)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            log.error("Error deleting subscriptions for user: {}", userId, e)
            throw SubscriptionException("Error deleting subscriptions for user", e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SubscriptionStoreR2dbcRepository::class.java)
    }
}
