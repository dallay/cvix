package com.cvix.form.infrastructure.persistence.repository

import com.cvix.form.infrastructure.persistence.entity.SubscriptionFormEntity
import com.cvix.spring.boot.repository.ReactiveSearchRepository
import java.util.UUID
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = true)
interface SubscriptionFormReactiveR2dbcRepository :
    CoroutineCrudRepository<SubscriptionFormEntity, UUID>,
    ReactiveSearchRepository<SubscriptionFormEntity> {

    /**
     * Finds a subscription form by ID and workspace ID.
     */
    suspend fun findByIdAndWorkspaceId(id: UUID, workspaceId: UUID): SubscriptionFormEntity?
}
