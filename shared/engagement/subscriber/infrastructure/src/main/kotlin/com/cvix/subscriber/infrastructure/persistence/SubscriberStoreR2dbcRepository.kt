package com.cvix.subscriber.infrastructure.persistence

import com.cvix.common.domain.criteria.Criteria
import com.cvix.common.domain.model.pagination.CursorPage
import com.cvix.common.domain.model.pagination.OffsetPage
import com.cvix.common.domain.presentation.pagination.Cursor
import com.cvix.spring.boot.presentation.sort.toSpringSort
import com.cvix.spring.boot.repository.R2DBCCriteriaParser
import com.cvix.subscriber.domain.Subscriber
import com.cvix.subscriber.domain.SubscriberRepository
import com.cvix.subscriber.domain.SubscriberSearchRepository
import com.cvix.subscriber.domain.SubscriberStatus
import com.cvix.subscriber.infrastructure.persistence.entity.SubscriberEntity
import com.cvix.subscriber.infrastructure.persistence.mapper.SubscriberMapper.toDomain
import com.cvix.subscriber.infrastructure.persistence.mapper.SubscriberMapper.toEntity
import com.cvix.subscriber.infrastructure.persistence.repository.SubscriberReactiveR2dbcRepository
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import com.cvix.common.domain.presentation.sort.Sort as DomainSort

/**
 * R2DBC implementation of Subscriber repositories.
 *
 * This class acts as an adapter that implements both [SubscriberRepository] (write)
 * and [SubscriberSearchRepository] (read) using Spring Data R2DBC.
 *
 * @property subscriberReactiveR2dbcRepository The underlying Spring Data R2DBC repository.
 */
@Repository
@Transactional("connectionFactoryTransactionManager")
class SubscriberStoreR2dbcRepository(
    private val subscriberReactiveR2dbcRepository: SubscriberReactiveR2dbcRepository,
) : SubscriberRepository, SubscriberSearchRepository {

    private val criteriaParser = R2DBCCriteriaParser(SubscriberEntity::class)

    override suspend fun create(subscriber: Subscriber) {
        log.debug("Creating subscriber with ID: {}", subscriber.id)
        try {
            subscriberReactiveR2dbcRepository.save(subscriber.toEntity())
            log.info("Successfully created subscriber with ID: {}", subscriber.id)
        } catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
            log.error("Failed to create subscriber with ID: {}", subscriber.id, error)
            throw error
        }
    }

    override suspend fun searchAllByOffset(
        criteria: Criteria?,
        size: Int?,
        page: Int?,
        sort: DomainSort?,
    ): OffsetPage<Subscriber> {
        log.debug("Searching subscribers by offset. size: {}, page: {}", size, page)
        val pageSize = size ?: DEFAULT_PAGE_SIZE
        val pageNumber = page ?: DEFAULT_PAGE_NUMBER
        val springSort = sort?.toSpringSort() ?: org.springframework.data.domain.Sort.unsorted()
        val pageable = PageRequest.of(pageNumber, pageSize, springSort)
        val springCriteria = criteria?.let { criteriaParser.parse(it) }
            ?: org.springframework.data.relational.core.query.Criteria.empty()

        val resultPage = subscriberReactiveR2dbcRepository.findAll(springCriteria, pageable, SubscriberEntity::class)

        return OffsetPage(
            data = resultPage.content.map { it.toDomain() },
            total = resultPage.totalElements,
            perPage = pageSize,
            page = pageNumber,
            totalPages = resultPage.totalPages,
        )
    }

    override suspend fun searchAllByCursor(
        criteria: Criteria?,
        size: Int?,
        sort: DomainSort?,
        cursor: Cursor?,
    ): CursorPage<Subscriber> {
        log.debug("Searching subscribers by cursor. size: {}", size)
        val pageSize = size ?: DEFAULT_PAGE_SIZE
        val springSort = sort?.toSpringSort() ?: org.springframework.data.domain.Sort.unsorted()
        val springCriteria = criteria?.let { criteriaParser.parse(it) }
            ?: org.springframework.data.relational.core.query.Criteria.empty()

        // If cursor is null, we might need a default one if the repository requires it
        // but typically ReactiveSearchRepository handles it or we provide a starting one.
        val currentCursor = cursor ?: com.cvix.common.domain.presentation.pagination.TimestampCursor.DEFAULT_CURSOR

        val result = subscriberReactiveR2dbcRepository.findAllByCursor(
            criteria = springCriteria,
            size = pageSize,
            domainType = SubscriberEntity::class,
            sort = springSort,
            cursor = currentCursor,
        )

        return CursorPage(
            data = result.data.map { it.toDomain() },
            prevPageCursor = result.prevPageCursor,
            nextPageCursor = result.nextPageCursor,
        )
    }

    @Transactional(transactionManager = "connectionFactoryTransactionManager", readOnly = true)
    override suspend fun searchActive(): List<Subscriber> {
        log.debug("Searching all active subscribers")
        return subscriberReactiveR2dbcRepository.findAllByStatus(SubscriberStatus.ENABLED)
            .map { it.toDomain() }
    }

    @Transactional(transactionManager = "connectionFactoryTransactionManager", readOnly = true)
    override suspend fun findById(id: UUID): Subscriber? {
        log.debug("Finding subscriber by ID: {}", id)
        return subscriberReactiveR2dbcRepository.findById(id)?.toDomain()
    }

    @Transactional(transactionManager = "connectionFactoryTransactionManager", readOnly = true)
    override suspend fun findByEmailAndSource(email: String, source: String): Subscriber? {
        log.debug("Finding subscriber by email and source: {}/{}", email, source)
        return subscriberReactiveR2dbcRepository.findByEmailAndSource(email, source)?.toDomain()
    }

    @Transactional(transactionManager = "connectionFactoryTransactionManager", readOnly = true)
    override suspend fun existsByEmailAndSource(email: String, source: String): Boolean {
        log.debug("Checking existence of subscriber by email and source: {}/{}", email, source)
        return subscriberReactiveR2dbcRepository.existsByEmailAndSource(email, source)
    }

    @Transactional(transactionManager = "connectionFactoryTransactionManager", readOnly = true)
    override suspend fun findAllByMetadata(key: String, value: String): List<Subscriber> {
        log.debug("Finding subscribers by metadata: {}={}", key, value)
        return subscriberReactiveR2dbcRepository.findAllByMetadata(key, value)
            .map { it.toDomain() }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SubscriberStoreR2dbcRepository::class.java)
        private const val DEFAULT_PAGE_SIZE = 10
        private const val DEFAULT_PAGE_NUMBER = 0
    }
}
