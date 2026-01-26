package com.cvix.form.application.find

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.query.QueryHandler
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.form.application.SubscriberFormResponse
import com.cvix.form.domain.SubscriptionFormId

/**
 * Handles the finding of a subscriber form.
 */
@Service
class FindSubscriberFormQueryHandler(
    private val workspaceAuthorization: WorkspaceAuthorization,
    private val formFinder: SubscriberFormFinder,
) : QueryHandler<FindSubscriberFormQuery, SubscriberFormResponse?> {

    override suspend fun handle(query: FindSubscriberFormQuery): SubscriberFormResponse? {
        workspaceAuthorization.ensureAccess(query.workspaceId, query.userId)

        return formFinder.find(
            SubscriptionFormId(query.formId),
            WorkspaceId(query.workspaceId),
        )?.let { SubscriberFormResponse.from(it) }
    }
}
