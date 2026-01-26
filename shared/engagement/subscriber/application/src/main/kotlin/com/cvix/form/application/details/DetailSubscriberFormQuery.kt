package com.cvix.form.application.details

import com.cvix.common.domain.bus.query.Query
import com.cvix.form.application.SubscriberFormResponse
import java.util.*

/**
 * Query to get detailed information about a subscriber form.
 * Expected to throw an exception if the form is not found.
 */
data class DetailSubscriberFormQuery(
    val formId: UUID,
    val workspaceId: UUID,
    val userId: UUID,
) : Query<SubscriberFormResponse>
