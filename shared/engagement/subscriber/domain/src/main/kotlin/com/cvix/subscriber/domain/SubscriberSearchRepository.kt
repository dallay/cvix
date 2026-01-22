package com.cvix.subscriber.domain

import com.cvix.common.domain.criteria.Criteria
import com.cvix.common.domain.model.pagination.CursorPage
import com.cvix.common.domain.model.pagination.OffsetPage
import com.cvix.common.domain.presentation.pagination.Cursor
import com.cvix.common.domain.presentation.sort.Sort
import java.util.UUID

/**
 * Repository interface for searching and retrieving Subscriber domain objects.
 *
 * Provides methods for paginated and filtered search operations, as well as
 * lookups by unique identifiers and metadata. Designed for use with coroutines
 * and non-blocking IO in a reactive context.
 */
interface SubscriberSearchRepository {
    /**
     * Searches for subscribers using offset-based pagination.
     *
     * @param criteria Optional search criteria for filtering results.
     * @param size Optional page size (number of results per page).
     * @param page Optional page number (zero-based).
     * @param sort Optional sort order for the results.
     * @return [OffsetPage] containing a page of [Subscriber]s.
     */
    suspend fun searchAllByOffset(
        criteria: Criteria? = null,
        size: Int? = null,
        page: Int? = null,
        sort: Sort? = null,
    ): OffsetPage<Subscriber>

    /**
     * Searches for subscribers using cursor-based pagination.
     *
     * @param criteria Optional search criteria for filtering results.
     * @param size Optional page size (number of results per page).
     * @param sort Optional sort order for the results.
     * @param cursor Optional cursor for pagination.
     * @return [CursorPage] containing a page of [Subscriber]s.
     */
    suspend fun searchAllByCursor(
        criteria: Criteria? = null,
        size: Int? = null,
        sort: Sort? = null,
        cursor: Cursor? = null,
    ): CursorPage<Subscriber>

    /**
     * Retrieves all active subscribers.
     *
     * @return List of active [Subscriber]s.
     */
    suspend fun searchActive(): List<Subscriber>

    /**
     * Finds a subscriber by their unique identifier.
     *
     * @param id The [UUID] of the subscriber.
     * @return The [Subscriber] if found, or null otherwise.
     */
    suspend fun findById(id: UUID): Subscriber?

    /**
     * Finds a subscriber by email and source.
     *
     * @param email The email address of the subscriber.
     * @param source The source associated with the subscriber.
     * @return The [Subscriber] if found, or null otherwise.
     */
    suspend fun findByEmailAndSource(email: String, source: String): Subscriber?

    /**
     * Checks if a subscriber exists by email and source.
     *
     * @param email The email address to check.
     * @param source The source to check.
     * @return `true` if a subscriber exists, `false` otherwise.
     */
    suspend fun existsByEmailAndSource(email: String, source: String): Boolean

    /**
     * Finds all subscribers matching a specific metadata key-value pair.
     *
     * @param key The metadata key.
     * @param value The metadata value.
     * @return List of [Subscriber]s matching the metadata.
     */
    suspend fun findAllByMetadata(key: String, value: String): List<Subscriber>
}
