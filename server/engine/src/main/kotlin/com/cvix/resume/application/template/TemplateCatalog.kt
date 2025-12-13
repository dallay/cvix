package com.cvix.resume.application.template

import com.cvix.common.domain.Service
import com.cvix.resume.domain.TemplateMetadata
import com.cvix.resume.domain.TemplateSourceStrategy
import com.cvix.subscription.domain.SubscriptionTier
import org.slf4j.LoggerFactory

/**
 * Catalog service for managing and retrieving template metadata.
 *
 * Uses TemplateSourceStrategy to resolve the appropriate repositories at runtime.
 * This allows dynamic template loading from multiple sources (classpath, filesystem, S3, database)
 * without coupling the application layer to infrastructure details.
 *
 * The strategy pattern enables context-aware template loading based on user plans:
 * - Free users: Only classpath (bundled) templates marked as FREE tier
 * - Premium users: Classpath + additional sources (filesystem, S3) with BASIC/PROFESSIONAL templates
 * - Enterprise users: All sources + custom tenant-specific templates
 *
 * @created 26/11/25
 */
@Service
class TemplateCatalog(private val templateSourceStrategy: TemplateSourceStrategy) {
    /**
     * Lists available templates with an optional limit, filtered by user's subscription tier.
     *
     * Retrieves templates from all active repositories based on the current user's subscription context.
     * Only returns templates that the user's subscription tier can access.
     *
     * For example:
     * - FREE users: See only FREE tier templates
     * - BASIC users: See FREE + BASIC tier templates
     * - PROFESSIONAL users: See FREE + BASIC + PROFESSIONAL tier templates
     *
     * Templates are deduplicated by ID, with priority given to repositories in order
     * (first repository wins in case of ID conflicts). This allows premium templates
     * to override default ones when they have the same ID.
     *
     * Error handling: If a repository fails to load, it's logged and skipped,
     * allowing the application to continue with other sources.
     *
     * @param subscriptionTier The user's subscription tier for filtering accessible templates
     * @param limit Maximum number of templates to return (null = no limit)
     * @return List of template metadata from all active sources, filtered by subscription tier
     */
    suspend fun listTemplates(
        subscriptionTier: SubscriptionTier,
        limit: Int?
    ): List<TemplateMetadata> {
        log.debug("Fetching templates for tier={} with limit={}", subscriptionTier, limit)

        // Get all active repositories for the current user context
        val activeRepositories = templateSourceStrategy.activeTemplateRepositories(subscriptionTier)
        log.debug(
            "Found {} active template repositories for tier {}",
            activeRepositories.size,
            subscriptionTier,
        )

        // Load templates from all active repositories and deduplicate by ID
        val allTemplates = activeRepositories
            .flatMap { repository ->
                try {
                    repository.findAll().also { templates ->
                        log.debug(
                            "Loaded {} templates from {}",
                            templates.size,
                            repository::class.simpleName,
                        )
                    }
                } catch (e: RuntimeException) {
                    log.warn(
                        "Failed to load templates from {}: {}",
                        repository::class.simpleName,
                        e.message,
                        e,
                    )
                    emptyList()
                }
            }
            .distinctBy { it.id } // Deduplicate by ID (first repository wins)

        // Filter templates by subscription tier
        val accessibleTemplates = allTemplates.filter { template ->
            val isAccessible = template.isAccessibleBy(subscriptionTier)
            if (!isAccessible) {
                log.debug(
                    "Template {} requires tier {} but user has {}",
                    template.id,
                    template.requiredSubscriptionTier,
                    subscriptionTier,
                )
            }
            isAccessible
        }

        log.debug(
            "Total templates after tier filtering: {} (accessible by {})",
            accessibleTemplates.size,
            subscriptionTier,
        )

        return limit
            ?.takeIf { it > 0 }
            ?.let { accessibleTemplates.take(it) }
            ?: accessibleTemplates
    }

    companion object {
        private val log = LoggerFactory.getLogger(TemplateCatalog::class.java)
    }
}
