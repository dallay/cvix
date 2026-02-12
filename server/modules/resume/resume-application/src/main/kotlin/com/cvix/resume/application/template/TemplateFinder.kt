package com.cvix.resume.application.template

import com.cvix.common.domain.Service
import com.cvix.resume.domain.TemplateMetadata
import com.cvix.resume.domain.TemplateSourceStrategy
import com.cvix.resume.domain.exception.TemplateAccessDeniedException
import com.cvix.resume.domain.exception.TemplateNotFoundException
import com.cvix.subscription.domain.SubscriptionTier
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable

/**
 * Service for finding and validating template access.
 *
 * Encapsulates the logic for resolving templates by ID and validating user permissions
 * based on subscription tier. This service uses [TemplateSourceStrategy] to support
 * multiple data sources for template resolution, enabling scenarios such as multi-source,
 * multi-tenant, or fallback template discovery.
 *
 * @created 14/12/25
 */
@Service
class TemplateFinder(
    private val templateSourceStrategy: TemplateSourceStrategy,
) {
    /**
     * Finds a template by ID and validates that the user has permission to use it.
     *
     * Searches across all active template repositories based on the user's subscription tier
     * until the template is found. This supports multi-source scenarios where templates may be
     * located in different data sources (e.g., classpath, filesystem, S3).
     *
     * @param templateId The ID of the template to find
     * @param userId The ID of the user requesting the template
     * @param userTier The user's subscription tier
     * @return The template metadata if found and user has permission
     * @throws TemplateNotFoundException if template not found in any active repository
     * @throws TemplateAccessDeniedException if user lacks required subscription tier
     */
    // Performance: Cache template details and access validation results.
    // Keyed by template and tier; userId is excluded as access is strictly tier-based.
    @Cacheable(value = ["template-details"], key = "#templateId + '-' + #userTier.toString()")
    suspend fun findByIdAndValidateAccess(
        templateId: String,
        userId: UUID,
        userTier: SubscriptionTier
    ): TemplateMetadata {
        log.debug("Finding template - templateId={}, userId={}, userTier={}", templateId, userId, userTier)

        // Step 1: Get active template repositories for this subscription tier
        val activeRepositories = templateSourceStrategy.activeTemplateRepositories()
        log.debug(
            "Active template repositories - userId={}, tier={}, count={}",
            userId,
            userTier,
            activeRepositories.size,
        )

        // Step 2: Search for template across all active repositories
        var templateMetadata: TemplateMetadata? = null
        for (repository in activeRepositories) {
            templateMetadata = repository.findById(templateId)
            if (templateMetadata != null) {
                log.debug(
                    "Template found in repository - templateId={}, repositoryType={}",
                    templateId,
                    repository::class.simpleName,
                )
                break
            }
        }

        if (templateMetadata == null) {
            log.warn(
                "Template not found in any active repository - templateId={}, userId={}, tier={}",
                templateId,
                userId,
                userTier,
            )
            throw TemplateNotFoundException(templateId)
        }

        log.debug(
            "Validating template access - templateId={}, userId={}, requiredTier={}, userTier={}",
            templateId,
            userId,
            templateMetadata.requiredSubscriptionTier,
            userTier,
        )

        // Step 3: Validate user has required subscription tier
        if (!templateMetadata.isAccessibleBy(userTier)) {
            log.warn(
                "Template access denied - templateId={}, userId={}, requiredTier={}, userTier={}",
                templateId,
                userId,
                templateMetadata.requiredSubscriptionTier,
                userTier,
            )
            throw TemplateAccessDeniedException(
                templateId = templateId,
                requiredTier = templateMetadata.requiredSubscriptionTier,
                userTier = userTier,
            )
        }

        log.debug("Template access granted - templateId={}, userId={}", templateId, userId)
        return templateMetadata
    }

    companion object {
        private val log = LoggerFactory.getLogger(TemplateFinder::class.java)
    }
}
