package com.cvix.form.application.details

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.query.QueryHandler
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.form.application.SubscriberFormResponse
import com.cvix.form.domain.SubscriptionFormId

/**
 * Handles the retrieval of subscriber form details.
 */
@Service
class DetailSubscriberFormQueryHandler(
    private val workspaceAuthorization: WorkspaceAuthorization,
    private val formFetcher: DetailSubscriberFormFetcher,
) : QueryHandler<DetailSubscriberFormQuery, SubscriberFormResponse> {

    override suspend fun handle(query: DetailSubscriberFormQuery): SubscriberFormResponse {
        workspaceAuthorization.ensureAccess(query.workspaceId, query.userId)

        return SubscriberFormResponse.from(
            formFetcher.fetch(
                SubscriptionFormId(query.formId),
                WorkspaceId(query.workspaceId),
            ),
        )
    }
}
