package com.cvix.resume.domain

import com.cvix.subscription.domain.SubscriptionTier

/**
 * Template metadata representing available PDF template configurations.
 * Used by the PDF Generation screen to display template options.
 * @param id Template identifier
 * @param name Template name
 * @param version Template version
 * @param descriptions Localized descriptions of the template
 * @param supportedLocales List of supported locales for this template
 * @param templatePath Optional path to the template resources
 * @param previewUrl Optional URL to a preview image of the template
 * @param params Optional template parameters for customization
 * @param requiredSubscriptionTier The minimum subscription tier required to access this template.
 *                                 Defaults to FREE so all templates are accessible by default.
 *                                 Set to BASIC or PROFESSIONAL to create premium templates.
 * @created 26/11/25
 */
data class TemplateMetadata(
    val id: String,
    val name: String,
    val version: String,
    val descriptions: Map<Locale, String> = emptyMap(),
    val supportedLocales: List<Locale> = emptyList(),
    val templatePath: String,
    val previewUrl: String? = null,
    val params: TemplateParams? = null,
    val requiredSubscriptionTier: SubscriptionTier = SubscriptionTier.FREE
) {
    /**
     * Checks if a user with the given subscription tier can access this template.
     *
     * @param userTier The user's subscription tier
     * @return true if the user's tier is at least as high as the required tier, false otherwise
     */
    fun isAccessibleBy(userTier: SubscriptionTier): Boolean =
        SubscriptionTier.isAtLeastAs(userTier, requiredSubscriptionTier)
}

/**
 * Template parameters for customizing PDF output.
 * Validated against the template's paramsSchema.
 */
data class TemplateParams(
    val colorPalette: String? = null,
    val fontFamily: String? = null,
    val spacing: String? = null,
    val density: String? = null,
    val customParams: Map<String, Any> = emptyMap(),
)
