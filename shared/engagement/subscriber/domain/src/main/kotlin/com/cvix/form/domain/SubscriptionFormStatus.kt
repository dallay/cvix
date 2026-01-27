package com.cvix.form.domain

/**
 * Represents the status of a subscription form.
 *
 * - [DRAFT]: The form is being edited and not yet available for public use.
 * - [PUBLISHED]: The form is live and accepting submissions.
 * - [DISABLED]: The form is temporarily disabled by the owner.
 * - [ARCHIVED]: The form is archived and no longer in use.
 */
enum class SubscriptionFormStatus {
    DRAFT,
    PUBLISHED,
    DISABLED,
    ARCHIVED,
}
