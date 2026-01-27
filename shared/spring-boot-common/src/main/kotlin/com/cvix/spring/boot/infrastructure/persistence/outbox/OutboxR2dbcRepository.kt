package com.cvix.spring.boot.infrastructure.persistence.outbox

import java.util.UUID
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface OutboxR2dbcRepository : CoroutineCrudRepository<OutboxEntity, UUID>
