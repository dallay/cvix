package com.cvix.form.domain

/**
 * Repository interface for managing [SubscriptionForm] entities.
 *
 * Provides methods for creating, updating, and deleting subscription forms.
 *
 * @created 25/1/26
 */
interface SubscriptionFormRepository {
    /**
     * Persists a new [SubscriptionForm] in the repository.
     *
     * @param form The subscription form to create.
     * @return The created [SubscriptionForm] with any generated fields populated.
     */
    suspend fun create(form: SubscriptionForm): SubscriptionForm

    /**
     * Updates an existing [SubscriptionForm] in the repository.
     *
     * @param form The subscription form with updated data.
     * @return The updated [SubscriptionForm].
     */
    suspend fun update(form: SubscriptionForm): SubscriptionForm

    /**
     * Deletes a [SubscriptionForm] from the repository by its identifier.
     *
     * @param id The unique identifier of the subscription form to delete.
     */
    suspend fun delete(id: SubscriptionFormId)
}
