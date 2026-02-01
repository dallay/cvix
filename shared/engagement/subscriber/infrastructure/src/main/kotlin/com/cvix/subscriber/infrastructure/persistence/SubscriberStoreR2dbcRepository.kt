package com.cvix.subscriber.infrastructure.persistence

import com.cvix.common.domain.criteria.Criteria
import com.cvix.common.domain.model.pagination.CursorPage
import com.cvix.common.domain.model.pagination.OffsetPage
import com.cvix.common.domain.presentation.pagination.Cursor
import com.cvix.common.domain.presentation.sort.Sort as DomainSort
import com.cvix.spring.boot.presentation.sort.toSpringSort
import com.cvix.spring.boot.repository.R2DBCCriteriaParser
import com.cvix.subscriber.domain.Subscriber
import com.cvix.subscriber.domain.SubscriberRepository
import com.cvix.subscriber.domain.SubscriberSearchRepository
import com.cvix.subscriber.domain.SubscriberStatus
import com.cvix.subscriber.infrastructure.persistence.entity.SubscriberEntity
import com.cvix.subscriber.infrastructure.persistence.mapper.SubscriberMapper
import com.cvix.subscriber.infrastructure.persistence.mapper.SubscriberMapper.toEntity
import com.cvix.subscriber.infrastructure.persistence.repository.SubscriberReactiveR2dbcRepository
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

/**
 * R2DBC implementation of Subscriber repositories.
 *
 * This class acts as an adapter that implements both [SubscriberRepository] (write)
 * and [SubscriberSearchRepository] (read) using Spring Data R2DBC.
 *
 * @property subscriberReactiveR2dbcRepository The underlying Spring Data R2DBC repository.
 * @property mapper The mapper for converting between entity and domain models.
 * @property transactionalOperator The transactional operator for managing reactive transactions.
 */
@Repository
class SubscriberStoreR2dbcRepository(
    private val subscriberReactiveR2dbcRepository: SubscriberReactiveR2dbcRepository,
    private val mapper: SubscriberMapper,
    private val transactionalOperator: TransactionalOperator,
) : SubscriberRepository, SubscriberSearchRepository {

    private val criteriaParser = R2DBCCriteriaParser(SubscriberEntity::class)

    override suspend fun create(subscriber: Subscriber): Unit = transactionalOperator.executeAndAwait {
        log.debug("Creating subscriber with ID: {}", subscriber.id)
        try {
            with(mapper) {
                subscriberReactiveR2dbcRepository.save(subscriber.toEntity())
            }
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
    ): OffsetPage<Subscriber> = transactionalOperator.executeAndAwait {
        log.debug("Searching subscribers by offset. size: {}, page: {}", size, page)
        val pageSize = size ?: DEFAULT_PAGE_SIZE
        val pageNumber = page ?: DEFAULT_PAGE_NUMBER
        val springSort = sort?.toSpringSort() ?: org.springframework.data.domain.Sort.unsorted()
        val pageable = PageRequest.of(pageNumber, pageSize, springSort)
        val springCriteria = criteria?.let { criteriaParser.parse(it) }
            ?: org.springframework.data.relational.core.query.Criteria.empty()

        val resultPage = subscriberReactiveR2dbcRepository.findAll(springCriteria, pageable, SubscriberEntity::class)

        with(mapper) {
            OffsetPage(
                data = resultPage.content.map { it.toDomain() },
                total = resultPage.totalElements,
                perPage = pageSize,
                page = pageNumber,
                totalPages = resultPage.totalPages,
            )
        }
    }

    override suspend fun searchAllByCursor(
        criteria: Criteria?,
        size: Int?,
        sort: DomainSort?,
        cursor: Cursor?,
    ): CursorPage<Subscriber> = transactionalOperator.executeAndAwait {
        log.debug("Searching subscribers by cursor. size: {}", size)
        val pageSize = size ?: DEFAULT_PAGE_SIZE
        val springSort = sort?.toSpringSort() ?: org.springframework.data.domain.Sort.unsorted()
        val springCriteria = criteria?.let { criteriaParser.parse(it) }
            ?: org.springframework.data.relational.core.query.Criteria.empty()

        val currentCursor = cursor ?: com.cvix.common.domain.presentation.pagination.TimestampCursor.DEFAULT_CURSOR

        val result = subscriberReactiveR2dbcRepository.findAllByCursor(
            criteria = springCriteria,
            size = pageSize,
            domainType = SubscriberEntity::class,
            sort = springSort,
            cursor = currentCursor,
        )

        with(mapper) {
            CursorPage(
                data = result.data.map { it.toDomain() },
                prevPageCursor = result.prevPageCursor,
                nextPageCursor = result.nextPageCursor,
            )
        }
    }

    override suspend fun searchActive(): List<Subscriber> = transactionalOperator.executeAndAwait {
        log.debug("Searching all active subscribers")
        with(mapper) {
            subscriberReactiveR2dbcRepository.findAllByStatus(SubscriberStatus.ENABLED)
                .map { it.toDomain() }
        }
    }

    override suspend fun findById(id: UUID): Subscriber? = transactionalOperator.executeAndAwait {
        log.debug("Finding subscriber by ID: {}", id)
        with(mapper) {
            subscriberReactiveR2dbcRepository.findById(id)?.toDomain()
        }
    }

    override suspend fun findByEmailAndSource(email: String, source: String): Subscriber? = transactionalOperator.executeAndAwait {
        log.debug("Finding subscriber by email and source: {}/{}", email, source)
        with(mapper) {
            subscriberReactiveR2dbcRepository.findByEmailAndSource(email, source)?.toDomain()
        }
    }

    override suspend fun existsByEmailAndSource(email: String, source: String): Boolean = transactionalOperator.executeAndAwait {
        log.debug("Checking existence of subscriber by email and source: {}/{}", email, source)
        subscriberReactiveR2dbcRepository.existsByEmailAndSource(email, source)
    }

    override suspend fun findAllByMetadata(key: String, value: String): List<Subscriber> = transactionalOperator.executeAndAwait {
        log.debug("Finding subscribers by metadata: {}={}", key, value)
        with(mapper) {
            subscriberReactiveR2dbcRepository.findAllByMetadata(key, value)
                .map { it.toDomain() }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SubscriberStoreR2dbcRepository::class.java)
        private const val DEFAULT_PAGE_SIZE = 10
        private const val DEFAULT_PAGE_NUMBER = 0
    }
}
