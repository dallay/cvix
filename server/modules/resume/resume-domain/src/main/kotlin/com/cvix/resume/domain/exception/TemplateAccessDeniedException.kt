package com.cvix.resume.domain.exception

import com.cvix.subscription.domain.SubscriptionTier

/**
 * Exception thrown when a requested template is not found in any template repository.
 *
 * This exception should be mapped to HTTP 404 Not Found in the infrastructure layer.
 *
 * @param templateId The ID of the template that was requested
 * @created 12/12/25
 */
class TemplateNotFoundException(
    val templateId: String
) : ResumeGenerationException(
    "Template not found: $templateId",
)

/**
 * Exception thrown when a user attempts to access a template that requires
 * a higher subscription tier than they currently have.
 *
 * This exception should be mapped to HTTP 403 Forbidden in the infrastructure layer.
 *
 * @param templateId The ID of the template that was requested
 * @param requiredTier The subscription tier required to access the template
 * @param userTier The user's current subscription tier
 * @created 12/12/25
 */
class TemplateAccessDeniedException(
    val templateId: String,
    val requiredTier: SubscriptionTier,
    val userTier: SubscriptionTier
) : ResumeGenerationException(
    "Access denied to template '$templateId'. Required tier: $requiredTier, User tier: $userTier",
)
