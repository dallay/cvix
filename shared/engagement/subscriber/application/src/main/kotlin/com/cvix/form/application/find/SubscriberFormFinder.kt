package com.cvix.form.application.find

import com.cvix.common.domain.Service
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.form.domain.SubscriptionForm
import com.cvix.form.domain.SubscriptionFormFinderRepository
import com.cvix.form.domain.SubscriptionFormId

/**
 * Application service for finding subscription forms.
 */
@Service
class SubscriberFormFinder(
    private val formFinderRepository: SubscriptionFormFinderRepository,
) {
    /**
     * Finds a subscription form by ID and workspace ID.
     */
    suspend fun find(formId: SubscriptionFormId, workspaceId: WorkspaceId): SubscriptionForm? =
        formFinderRepository.findByFormIdAndWorkspaceId(formId, workspaceId)
}
