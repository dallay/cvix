package com.cvix.form.application.search

import com.cvix.common.domain.bus.query.Query
import com.cvix.common.domain.criteria.Criteria
import com.cvix.common.domain.presentation.pagination.CursorPageResponse
import com.cvix.common.domain.presentation.sort.Sort
import com.cvix.form.application.SubscriberFormResponse
import java.util.*

/**
 * Query to search for subscriber forms.
 */
data class SearchSubscriberFormsQuery(
    val workspaceId: UUID,
    val userId: UUID,
    val criteria: Criteria? = null,
    val size: Int? = null,
    val cursor: String? = null,
    val sort: Sort? = null
) : Query<CursorPageResponse<SubscriberFormResponse>>
