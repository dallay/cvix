package com.cvix.subscription.domain

import java.util.*

/**
 * Repository interface for subscription persistence operations.
 *
 * This is a port in hexagonal architecture, defining the contract
 * for subscription data access without exposing implementation details.
 *
 * @created 12/11/25
 */
interface SubscriptionRepository {
    /**
     * Finds a subscription by its unique identifier.
     *
     * @param id The subscription identifier
     * @return The subscription if found, null otherwise
     */
    suspend fun findById(id: SubscriptionId): Subscription?

    /**
     * Finds the active subscription for a given user.
     *
     * Only returns subscriptions with ACTIVE status.
     * If multiple active subscriptions exist (should not happen),
     * returns the most recent one.
     *
     * @param userId The user UUID
     * @return The active subscription if found, null otherwise
     */
    suspend fun findActiveByUserId(userId: UUID): Subscription?

    /**
     * Finds all subscriptions for a given user (active and inactive).
     *
     * @param userId The user UUID
     * @return List of all subscriptions for the user, ordered by validFrom descending
     */
    suspend fun findAllByUserId(userId: UUID): List<Subscription>

    /**
     * Saves a subscription (create or update).
     *
     * @param subscription The subscription to save
     */
    suspend fun save(subscription: Subscription)

    /**
     * Deletes a subscription.
     *
     * @param id The subscription identifier
     */
    suspend fun delete(id: SubscriptionId)
}
