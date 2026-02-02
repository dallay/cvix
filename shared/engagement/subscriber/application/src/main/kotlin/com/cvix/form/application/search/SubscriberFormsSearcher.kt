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
            "Searching forms with size={}, cursor={}, sort={}, filters={}",
            size,
            cursor,
            sort,
            sanitizeCriteria(criteria),
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

        // UUID v4/v1 pattern: 8-4-4-4-12 hex groups
        private val UUID_REGEX = Regex(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
        )

        // Comprehensive email pattern including plus-addressing and subdomains
        private val EMAIL_REGEX = Regex(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9][a-zA-Z0-9.-]*\\.[a-zA-Z]{2,}",
        )

        private fun sanitizeCriteria(criteria: Criteria?): String {
            if (criteria == null) return "{}"
            // Redact potential PII from criteria string representation
            // Run email masking first to avoid accidental overlaps with ID masking
            return criteria.toString()
                .replace(EMAIL_REGEX, "<masked-email>")
                .replace(UUID_REGEX, "<masked-id>")
        }
    }
}
