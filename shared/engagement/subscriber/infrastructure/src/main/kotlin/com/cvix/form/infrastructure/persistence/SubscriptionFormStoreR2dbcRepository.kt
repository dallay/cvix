package com.cvix.form.infrastructure.persistence

import com.cvix.common.domain.criteria.Criteria
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.presentation.pagination.Cursor
import com.cvix.common.domain.presentation.pagination.CursorPageResponse
import com.cvix.common.domain.presentation.pagination.TimestampCursor
import com.cvix.common.domain.presentation.sort.Sort
import com.cvix.form.domain.SubscriptionForm
import com.cvix.form.domain.SubscriptionFormFinderRepository
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormRepository
import com.cvix.form.infrastructure.persistence.entity.SubscriptionFormEntity
import com.cvix.form.infrastructure.persistence.mapper.SubscriptionFormMapper
import com.cvix.form.infrastructure.persistence.repository.SubscriptionFormReactiveR2dbcRepository
import com.cvix.spring.boot.presentation.sort.toSpringSort
import com.cvix.spring.boot.repository.R2DBCCriteriaParser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Repository
class SubscriptionFormStoreR2dbcRepository(
    private val subscriptionFormReactiveR2dbcRepository: SubscriptionFormReactiveR2dbcRepository,
    private val mapper: SubscriptionFormMapper,
    private val transactionalOperator: TransactionalOperator,
) : SubscriptionFormRepository, SubscriptionFormFinderRepository {

    private val criteriaParser = R2DBCCriteriaParser(SubscriptionFormEntity::class)

    override suspend fun create(form: SubscriptionForm): SubscriptionForm = with(mapper) {
        log.debug("Creating subscription form with ID: {}", form.id)
        subscriptionFormReactiveR2dbcRepository.save(form.toEntity()).toDomain()
    }

    override suspend fun update(form: SubscriptionForm): SubscriptionForm = with(mapper) {
        log.debug("Updating subscription form with ID: {}", form.id)
        subscriptionFormReactiveR2dbcRepository.save(form.toEntity()).toDomain()
    }

    override suspend fun delete(id: SubscriptionFormId) {
        log.debug("Deleting subscription form with ID: {}", id)
        subscriptionFormReactiveR2dbcRepository.deleteById(id.value)
    }

    override suspend fun findById(id: SubscriptionFormId): SubscriptionForm? = transactionalOperator.executeAndAwait {
        log.debug("Finding subscription form by ID: {}", id)
        with(mapper) {
            subscriptionFormReactiveR2dbcRepository.findById(id.value)?.toDomain()
        }
    }

    override suspend fun findByFormIdAndWorkspaceId(
        formId: SubscriptionFormId,
        workspaceId: WorkspaceId
    ): SubscriptionForm? = transactionalOperator.executeAndAwait {
        log.debug("Finding subscription form by ID: {} and Workspace ID: {}", formId, workspaceId)
        with(mapper) {
            subscriptionFormReactiveR2dbcRepository.findByIdAndWorkspaceId(
                formId.value,
                workspaceId.value,
            )?.toDomain()
        }
    }

    override suspend fun search(
        criteria: Criteria?,
        size: Int?,
        sort: Sort?,
        cursor: Cursor?
    ): CursorPageResponse<SubscriptionForm> = transactionalOperator.executeAndAwait {
        log.debug(
            "Searching subscription forms with criteria: {}, size: {}, sort: {}, cursor: {}",
            criteria,
            size,
            sort,
            cursor,
        )
        val pageSize = size ?: DEFAULT_PAGE_SIZE
        val springSort = sort?.toSpringSort() ?: org.springframework.data.domain.Sort.unsorted()
        val springCriteria = criteria?.let { criteriaParser.parse(it) }
            ?: org.springframework.data.relational.core.query.Criteria.empty()

        val currentCursor = cursor ?: TimestampCursor.DEFAULT_CURSOR

        val result = subscriptionFormReactiveR2dbcRepository.findAllByCursor(
            criteria = springCriteria,
            size = pageSize,
            domainType = SubscriptionFormEntity::class,
            sort = springSort,
            cursor = currentCursor,
        )

        with(mapper) {
            CursorPageResponse(
                data = result.data.map { it.toDomain() },
                prevPageCursor = result.prevPageCursor,
                nextPageCursor = result.nextPageCursor,
            )
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SubscriptionFormStoreR2dbcRepository::class.java)
        private const val DEFAULT_PAGE_SIZE = 20
    }
}
