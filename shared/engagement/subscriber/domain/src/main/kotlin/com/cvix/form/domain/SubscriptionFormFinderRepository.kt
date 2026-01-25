package com.cvix.form.domain

import com.cvix.common.domain.criteria.Criteria
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.presentation.pagination.Cursor
import com.cvix.common.domain.presentation.pagination.CursorPageResponse
import com.cvix.common.domain.presentation.sort.Sort

/**
 * Repository interface responsible for finding and searching [SubscriptionForm] entities.
 *
 * Mirrors the API of other finder repositories in the codebase and exposes methods for
 * retrieving forms by id, by form and workspace, and searching with cursor-based pagination.
 *
 * @created 25/1/26
 */
interface SubscriptionFormFinderRepository {

    /**
     * Find a subscription form by its identifier.
     *
     * @param id The [SubscriptionFormId] to look up.
     * @return The [SubscriptionForm] if found, or null otherwise.
     */
    suspend fun findById(id: SubscriptionFormId): SubscriptionForm?

    /**
     * Find a subscription form by form id and workspace id.
     *
     * @param formId The [SubscriptionFormId] of the form.
     * @param workspaceId The [WorkspaceId] the form belongs to.
     * @return The [SubscriptionForm] if found, or null otherwise.
     */
    suspend fun findByFormIdAndWorkspaceId(
        formId: SubscriptionFormId,
        workspaceId: WorkspaceId
    ): SubscriptionForm?

    /**
     * Search for subscription forms using cursor-based pagination.
     *
     * @param criteria Optional [Criteria] to filter results.
     * @param size Optional page size (max enforced by implementation).
     * @param sort Optional [Sort] order for the results.
     * @param cursor Optional [Cursor] for pagination.
     * @return A [CursorPageResponse] containing a page of [SubscriptionForm] results.
     */
    suspend fun search(
        criteria: Criteria? = null,
        size: Int? = null,
        sort: Sort? = null,
        cursor: Cursor? = null,
    ): CursorPageResponse<SubscriptionForm>
}
