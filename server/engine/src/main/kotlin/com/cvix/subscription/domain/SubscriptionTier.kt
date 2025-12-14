package com.cvix.subscription.domain

/**
 * Represents the subscription tier/plan a user belongs to.
 *
 * Each tier defines:
 * - Rate limit capacity (requests per hour)
 * - Access to premium templates
 * - Priority and other features
 *
 * This enum is extensible: new plans can be added, and new features can be
 * queried without modifying existing code by adding new methods.
 *
 * @created 11/12/25
 */
enum class SubscriptionTier(val rank: Int) {
    FREE(1) {
        override fun getDisplayName(): String = "Free"
        override fun getRateLimitCapacity(): Long = 20L
        override fun canAccessPremiumTemplates(): Boolean = false
        override fun getMaxPremiumTemplates(): Int = 0
        override fun hasPriority(): Boolean = false
    },

    BASIC(2) {
        override fun getDisplayName(): String = "Basic"
        override fun getRateLimitCapacity(): Long = 40L
        override fun canAccessPremiumTemplates(): Boolean = true
        override fun getMaxPremiumTemplates(): Int = 5
        override fun hasPriority(): Boolean = false
    },

    PROFESSIONAL(3) {
        override fun getDisplayName(): String = "Professional"
        override fun getRateLimitCapacity(): Long = 100L
        override fun canAccessPremiumTemplates(): Boolean = true
        override fun getMaxPremiumTemplates(): Int = 100
        override fun hasPriority(): Boolean = true
    };

    /**
     * Returns the human-readable display name for this tier.
     * Useful for UI rendering and logging.
     */
    abstract fun getDisplayName(): String

    /**
     * Returns the rate limit capacity for this tier (requests per hour).
     * Used by the rate limiting system to configure Bucket4j limits.
     */
    abstract fun getRateLimitCapacity(): Long

    /**
     * Determines if this tier can access premium templates.
     * Premium templates are exclusive features available only to paying users.
     */
    abstract fun canAccessPremiumTemplates(): Boolean

    /**
     * Returns the maximum number of premium templates this tier can load.
     * Allows for plan-specific template quotas if needed in future.
     *
     * Returns 0 if premium templates are not accessible.
     */
    abstract fun getMaxPremiumTemplates(): Int

    /**
     * Determines if this tier gets priority processing.
     * Can be used for priority queues, faster processing, etc.
     */
    abstract fun hasPriority(): Boolean

    /**
     * Extensible method for checking custom features by name.
     * This allows adding new feature checks without modifying the enum.
     *
     * Examples of custom feature names:
     * - "pdf-export"
     * - "analytics"
     * - "api-access"
     * - "custom-branding"
     *
     * @param featureName The name of the feature to check
     * @return true if this tier has access to the feature, false otherwise
     */
    /**
     * Reserved extension point for future custom feature gates.
     * By default, returns false; override in a tier to enable custom features.
     */
    open fun hasFeature(featureName: String): Boolean = false

    companion object {
        /**
         * Returns the tier with the highest rank for comparison/sorting.
         */
        fun highestTier(): SubscriptionTier = PROFESSIONAL

        /**
         * Returns the tier with the lowest rank.
         */
        fun lowestTier(): SubscriptionTier = FREE

        /**
         * Checks if tier A has more or equal capabilities than tier B.
         */
        fun isAtLeastAs(tierA: SubscriptionTier, tierB: SubscriptionTier): Boolean =
            tierA.rank >= tierB.rank
    }
}
