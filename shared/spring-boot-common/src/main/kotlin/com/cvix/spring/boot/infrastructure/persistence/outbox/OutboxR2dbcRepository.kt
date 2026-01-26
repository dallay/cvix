package com.cvix.spring.boot.infrastructure.persistence.outbox

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface OutboxR2dbcRepository : CoroutineCrudRepository<OutboxEntity, UUID>
