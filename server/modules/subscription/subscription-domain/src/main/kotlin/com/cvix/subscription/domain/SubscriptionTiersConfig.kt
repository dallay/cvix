package com.cvix.subscription.domain

/**
 * Configuration properties for subscription tiers.
 *
 * This data class holds the rate limit and feature configuration for each subscription tier.
 * It can be externalized to application.yml for easy adjustment without code changes.
 *
 * @param tier The subscription tier
 * @param displayName Human-readable name for the tier
 * @param rateLimitCapacity Rate limit capacity (requests per hour)
 * @param premiumTemplatesEnabled Whether this tier can access premium templates
 * @param maxPremiumTemplates Maximum number of premium templates this tier can access
 * @param priority Whether this tier gets priority processing
 *
 * @created 11/12/25
 */
data class SubscriptionTierConfig(
    val tier: SubscriptionTier,
    val displayName: String,
    val rateLimitCapacity: Long,
    val premiumTemplatesEnabled: Boolean,
    val maxPremiumTemplates: Int,
    val priority: Boolean,
    val customFeatures: Map<String, Boolean> = emptyMap()
)

/**
 * Container for all subscription tier configurations.
 * Allows centralizing plan definitions for easier management.
 *
 * @param tiers Map of SubscriptionTier to its configuration
 */
data class SubscriptionTiersConfig(
    val tiers: Map<SubscriptionTier, SubscriptionTierConfig> = defaultConfigurations()
) {
    /**
     * Gets the configuration for a specific tier.
     * @param tier The subscription tier
     * @return The configuration for this tier, or the FREE tier config if not found
     */
    fun getConfig(tier: SubscriptionTier): SubscriptionTierConfig =
        tiers[tier] ?: tiers[SubscriptionTier.FREE]
            ?: error(
                "SubscriptionTiersConfig is missing the FREE tier configuration. This is required as a fallback.",
            )

    companion object {
        /**
         * Returns the default subscription tier configurations.
         * Can be overridden by external configuration (YAML, environment variables, database).
         */
        fun defaultConfigurations(): Map<SubscriptionTier, SubscriptionTierConfig> = mapOf(
            SubscriptionTier.FREE to SubscriptionTierConfig(
                tier = SubscriptionTier.FREE,
                displayName = "Free",
                rateLimitCapacity = 20L,
                premiumTemplatesEnabled = false,
                maxPremiumTemplates = 0,
                priority = false,
            ),
            SubscriptionTier.BASIC to SubscriptionTierConfig(
                tier = SubscriptionTier.BASIC,
                displayName = "Basic",
                rateLimitCapacity = 40L,
                premiumTemplatesEnabled = true,
                maxPremiumTemplates = 5,
                priority = false,
            ),
            SubscriptionTier.PROFESSIONAL to SubscriptionTierConfig(
                tier = SubscriptionTier.PROFESSIONAL,
                displayName = "Professional",
                rateLimitCapacity = 100L,
                premiumTemplatesEnabled = true,
                maxPremiumTemplates = 100,
                priority = true,
            ),
        )
    }
}
