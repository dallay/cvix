package com.cvix.resume.domain

import com.cvix.subscription.domain.SubscriptionTier

/**
 * Strategy port for selecting the active template repositories.
 *
 * This interface allows the application layer to retrieve all currently active
 * [TemplateRepository] instances, supporting scenarios such as multi-source,
 * multi-tenant, or fallback template discovery. Implementations can select repositories
 * based on configuration, context, or runtime conditions.
 *
 * This abstraction enables the application and domain layers to remain decoupled
 * from infrastructure details and repository selection logic.
 *
 * @created 11/12/25
 */
fun interface TemplateSourceStrategy {
    /**
     * Returns the list of active [TemplateRepository] instances to be used for template discovery.
     *
     * Implementations may return one or more repositories depending on the current context,
     * configuration, or business rules (e.g., classpath + filesystem, tenant-specific, etc).
     *
     * The subscription tier can be used to select different repositories for different user plans:
     * - Free users: Only classpath (bundled) templates
     * - Premium users: Classpath + additional sources (filesystem, S3)
     * - Enterprise users: All sources + custom tenant-specific templates
     *
     * @param subscriptionTier The user's subscription tier for context-aware repository selection
     * @return List of active [TemplateRepository]s
     */
    suspend fun activeTemplateRepositories(subscriptionTier: SubscriptionTier): List<TemplateRepository>
}
