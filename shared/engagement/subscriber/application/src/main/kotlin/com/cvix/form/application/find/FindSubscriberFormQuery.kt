package com.cvix.form.application.find

import com.cvix.common.domain.bus.query.Query
import com.cvix.form.application.SubscriberFormResponse
import java.util.*

/**
 * Query to find a subscriber form by its identifier and workspace.
 */
data class FindSubscriberFormQuery(
    val formId: UUID,
    val workspaceId: UUID,
    val userId: UUID,
) : Query<SubscriberFormResponse?>
