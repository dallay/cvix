package com.cvix.subscriber.domain

/**
 * Repository interface for managing [Subscriber] entities.
 *
 * Implementations of this interface are responsible for persisting and retrieving
 * subscriber data from the underlying data source.
 */
fun interface SubscriberRepository {
    /**
     * Persists a new [Subscriber] in the repository.
     *
     * @param subscriber The [Subscriber] entity to be created.
     */
    suspend fun create(subscriber: Subscriber)
}
