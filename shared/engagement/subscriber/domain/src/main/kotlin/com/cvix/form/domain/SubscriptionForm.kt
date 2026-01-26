package com.cvix.form.domain

import com.cvix.common.domain.SYSTEM_USER
import com.cvix.common.domain.model.BaseEntity
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.form.domain.event.SubscriptionFormCreatedEvent
import com.cvix.form.domain.event.SubscriptionFormUpdatedEvent
import java.time.Instant

/**
 * Domain entity representing a subscription form.
 *
 * @property id Unique identifier for the subscription form.
 * @property name Name of the subscription form.
 * @property description Description of the subscription form.
 * @property settings Customizable settings for the form's appearance and behavior.
 * @property status Current status of the subscription form (e.g., ACTIVE, ARCHIVED).
 * @property workspaceId Identifier of the workspace to which the form belongs.
 * @property createdAt Timestamp when the form was created.
 * @property createdBy User or system that created the form.
 * @property updatedAt Timestamp when the form was last updated, or null if never updated.
 * @property updatedBy User who last updated the form, or null if never updated.
 *
 * @constructor Creates a new [SubscriptionForm] entity.
 * @created 25/1/26
 */
data class SubscriptionForm(
    override val id: SubscriptionFormId,
    val name: String,
    val description: String,
    val settings: SubscriptionFormSettings,
    val status: SubscriptionFormStatus,
    val workspaceId: WorkspaceId,
    override val createdAt: Instant = Instant.now(),
    override val createdBy: String = SYSTEM_USER,
    override val updatedAt: Instant? = null,
    override val updatedBy: String? = null,
) : BaseEntity<SubscriptionFormId>() {

    /**
     * Return a copy of this form with status set to ARCHIVED and audit fields updated.
     * Throws if the form is already archived.
     * @param updatedBy actor performing the archive
     * @param now timestamp to use for the update (default Instant.now())
     */
    fun archive(updatedBy: String, now: Instant = Instant.now()): SubscriptionForm {
        require(status != SubscriptionFormStatus.ARCHIVED) { "SubscriptionForm is already archived" }

        val copy = this.copy(
            status = SubscriptionFormStatus.ARCHIVED,
            updatedAt = now,
            updatedBy = updatedBy,
        )

        return withUpdatedEvent(copy, now, updatedBy)
    }

    /**
     * Return a copy of this form with status set to ACTIVE and audit fields updated.
     * Throws if the form is already active.
     * @param updatedBy actor performing the activation
     * @param now timestamp to use for the update (default Instant.now())
     */
    fun activate(updatedBy: String, now: Instant = Instant.now()): SubscriptionForm {
        require(status != SubscriptionFormStatus.ACTIVE) { "SubscriptionForm is already active" }

        val copy = this.copy(
            status = SubscriptionFormStatus.ACTIVE,
            updatedAt = now,
            updatedBy = updatedBy,
        )

        return withUpdatedEvent(copy, now, updatedBy)
    }

    /**
     * Return a copy of this form with updated details and audit fields updated.
     * Performs basic validation of inputs.
     * @param name new name
     * @param description new description
     * @param settings new settings
     * @param updatedBy actor performing the update
     * @param now timestamp to use for the update (default Instant.now())
     */
    fun updateDetails(
        name: String,
        description: String,
        settings: SubscriptionFormSettings,
        updatedBy: String,
        now: Instant = Instant.now(),
    ): SubscriptionForm {
        require(name.isNotBlank()) { "SubscriptionForm name cannot be blank" }

        val copy = this.copy(
            name = name,
            description = description,
            settings = settings,
            updatedAt = now,
            updatedBy = updatedBy,
        )

        return withUpdatedEvent(copy, now, updatedBy)
    }

    /**
     * Helper that records a SubscriptionFormUpdatedEvent on the provided copy and returns it.
     * Extracted to avoid duplicating event creation logic across mutating operations.
     */
    private fun withUpdatedEvent(
        copy: SubscriptionForm,
        updatedAt: Instant,
        updatedBy: String
    ): SubscriptionForm {
        copy.record(
            SubscriptionFormUpdatedEvent(
                formId = copy.id,
                workspaceId = copy.workspaceId,
                updatedAt = updatedAt,
                updatedBy = updatedBy,
                payload = com.cvix.form.domain.event.SubscriptionFormPayload(
                    id = copy.id,
                    name = copy.name,
                    description = copy.description,
                ),
            ),
        )

        return copy
    }

    companion object {

        /**
         * Factory method to create a new [SubscriptionForm].
         *
         * @param id Unique identifier for the subscription form.
         * @param name Name of the subscription form.
         * @param description Description of the subscription form.
         * @param settings Customizable settings for the form's appearance and behavior.
         * @param workspaceId Identifier of the workspace to which the form belongs.
         * @param createdBy User or system that created the form.
         * @param createdAt Timestamp to use for creation (default Instant.now()).
         * @return A new instance of [SubscriptionForm].
         */
        fun create(
            id: SubscriptionFormId,
            name: String,
            description: String,
            settings: SubscriptionFormSettings,
            workspaceId: WorkspaceId,
            createdBy: String = SYSTEM_USER,
            createdAt: Instant = Instant.now(),
        ): SubscriptionForm = SubscriptionForm(
            id = id,
            name = name,
            description = description,
            settings = settings,
            status = SubscriptionFormStatus.ACTIVE,
            workspaceId = workspaceId,
            createdAt = createdAt,
            createdBy = createdBy,
        ).also { form ->
            form.record(
                SubscriptionFormCreatedEvent(
                    formId = form.id,
                    workspaceId = form.workspaceId,
                    createdAt = form.createdAt,
                    createdBy = form.createdBy,
                    payload = form,
                ),
            )
        }
    }
}
