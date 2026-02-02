package com.cvix.common.domain.outbox

/**
 * Repository interface for managing [OutboxEntry] persistence.
 */
interface OutboxRepository {
    /**
     * Saves a new outbox entry.
     */
    suspend fun save(entry: OutboxEntry)
}
