package com.cvix.common.domain.outbox

import com.cvix.common.domain.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory

/**
 * In-memory implementation of [OutboxRepository] for testing and development purposes.
 */
@Service
class InMemoryOutboxRepository : OutboxRepository {
    private val entries = ConcurrentHashMap<UUID, OutboxEntry>()

    override suspend fun save(entry: OutboxEntry) {
        entries[entry.id] = entry
        log.debug("Saved outbox entry: {}", entry.id)
    }

    companion object {
        private val log = LoggerFactory.getLogger(InMemoryOutboxRepository::class.java)
    }
}
