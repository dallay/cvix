package com.cvix.ratelimit.infrastructure

import com.cvix.subscription.domain.SubscriptionTier
import io.github.bucket4j.Bandwidth
import java.time.Duration.ofHours

/**
 * Enum representing different pricing plans for API rate limiting.
 * Each plan defines its own rate limit (Bandwidth).
 *
 * **Note**: This class is being gradually migrated to use [SubscriptionTier] as the source of truth
 * for plan capabilities. New code should prefer using [SubscriptionTier] directly.
 *
 * Current status:
 * - Rate limits are still defined here but derived from [SubscriptionTier]
 * - Future: All plan logic will move to [SubscriptionTier] and this will become a thin adapter
 *
 * @see SubscriptionTier for the new plan system
 * @since 2.0.0
 * @deprecated Use [SubscriptionTier] for new functionality
 */
enum class PricingPlan {
    FREE {
        override fun getLimit(): Bandwidth = Bandwidth.builder()
            .capacity(SubscriptionTier.FREE.getRateLimitCapacity())
            .refillGreedy(SubscriptionTier.FREE.getRateLimitCapacity(), ofHours(REFILL_HOURS))
            .build()

        override fun getSubscriptionTier(): SubscriptionTier = SubscriptionTier.FREE
    },
    BASIC {
        override fun getLimit(): Bandwidth = Bandwidth.builder()
            .capacity(SubscriptionTier.BASIC.getRateLimitCapacity())
            .refillGreedy(SubscriptionTier.BASIC.getRateLimitCapacity(), ofHours(REFILL_HOURS))
            .build()

        override fun getSubscriptionTier(): SubscriptionTier = SubscriptionTier.BASIC
    },
    PROFESSIONAL {
        override fun getLimit(): Bandwidth = Bandwidth.builder()
            .capacity(SubscriptionTier.PROFESSIONAL.getRateLimitCapacity())
            .refillGreedy(SubscriptionTier.PROFESSIONAL.getRateLimitCapacity(), ofHours(REFILL_HOURS))
            .build()

        override fun getSubscriptionTier(): SubscriptionTier = SubscriptionTier.PROFESSIONAL
    };

    abstract fun getLimit(): Bandwidth

    /**
     * Returns the corresponding [SubscriptionTier] for this pricing plan.
     * Enables access to all subscription-related features beyond just rate limiting.
     */
    abstract fun getSubscriptionTier(): SubscriptionTier

    companion object {
        // Refill window used by all plans (in hours)
        private const val REFILL_HOURS: Long = 1L

        /**
         * Resolves the pricing plan from a given API key.
         *
         * @param apiKey The API key.
         * @return The corresponding [PricingPlan].
         */
        fun resolvePlanFromApiKey(apiKey: String): PricingPlan = when {
            apiKey.startsWith("PX001-") -> PROFESSIONAL
            apiKey.startsWith("BX001-") -> BASIC
            else -> FREE
        }

        /**
         * Resolves the subscription tier from an API key.
         * Convenience method that wraps [resolvePlanFromApiKey].
         *
         * @param apiKey The API key.
         * @return The corresponding [SubscriptionTier].
         */
        fun resolveSubscriptionTierFromApiKey(apiKey: String): SubscriptionTier =
            resolvePlanFromApiKey(apiKey).getSubscriptionTier()
    }
}
