package com.cvix.form.application.search

import com.cvix.common.domain.Service
import com.cvix.common.domain.criteria.Criteria
import com.cvix.common.domain.presentation.pagination.CursorPageResponse
import com.cvix.common.domain.presentation.pagination.TimestampCursor
import com.cvix.common.domain.presentation.pagination.map
import com.cvix.common.domain.presentation.sort.Sort
import com.cvix.form.application.SubscriberFormResponse
import com.cvix.form.domain.SubscriptionFormFinderRepository
import org.slf4j.LoggerFactory

/**
 * Application service for searching subscription forms.
 */
@Service
class SubscriberFormsSearcher(
    private val formFinderRepository: SubscriptionFormFinderRepository,
) {
    /**
     * Searches for subscription forms based on the given criteria.
     */
    suspend fun search(
        criteria: Criteria?,
        size: Int?,
        cursor: String?,
        sort: Sort?,
    ): CursorPageResponse<SubscriberFormResponse> {
        log.debug(
            "Searching forms with criteria: {}, size: {}, cursor: {}, sort: {}",
            criteria,
            size,
            cursor,
            sort,
        )

        val timestampCursor = cursor?.let { TimestampCursor.deserialize(it) }

        return formFinderRepository.search(
            criteria = criteria,
            size = size,
            sort = sort,
            cursor = timestampCursor,
        ).map { forms ->
            forms.map { SubscriberFormResponse.from(it) }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SubscriberFormsSearcher::class.java)
    }
}
