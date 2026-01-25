package com.cvix.form.application.details

import com.cvix.common.domain.Service
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.form.domain.SubscriptionForm
import com.cvix.form.domain.SubscriptionFormFinderRepository
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.exception.SubscriptionFormNotFoundException

/**
 * Application service for fetching detailed subscription form information.
 */
@Service
class DetailSubscriberFormFetcher(
    private val formFinderRepository: SubscriptionFormFinderRepository,
) {
    /**
     * Fetches a subscription form by ID and workspace ID.
     * Throws [SubscriptionFormNotFoundException] if not found.
     */
    suspend fun fetch(formId: SubscriptionFormId, workspaceId: WorkspaceId): SubscriptionForm {
        return formFinderRepository.findByFormIdAndWorkspaceId(formId, workspaceId)
            ?: throw SubscriptionFormNotFoundException(
                "Subscription form with ID ${formId.value} not found in workspace ${workspaceId.value}",
            )
    }
}
