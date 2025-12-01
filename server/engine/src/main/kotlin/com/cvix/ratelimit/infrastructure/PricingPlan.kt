package com.cvix.ratelimit.infrastructure

import io.github.bucket4j.Bandwidth
import java.time.Duration.ofHours

/**
 * Enum representing different pricing plans for API rate limiting.
 * Each plan defines its own rate limit (Bandwidth).
 */
enum class PricingPlan {
    FREE {
        override fun getLimit(): Bandwidth = Bandwidth.builder()
            .capacity(FREE_CAPACITY)
            .refillGreedy(FREE_CAPACITY, ofHours(REFILL_HOURS))
            .build()
    },
    BASIC {
        override fun getLimit(): Bandwidth = Bandwidth.builder()
            .capacity(BASIC_CAPACITY)
            .refillGreedy(BASIC_CAPACITY, ofHours(REFILL_HOURS))
            .build()
    },
    PROFESSIONAL {
        override fun getLimit(): Bandwidth = Bandwidth.builder()
            .capacity(PROFESSIONAL_CAPACITY)
            .refillGreedy(PROFESSIONAL_CAPACITY, ofHours(REFILL_HOURS))
            .build()
    };

    abstract fun getLimit(): Bandwidth

    companion object {
        // Refill window used by all plans (in hours)
        private const val REFILL_HOURS: Long = 1L

        // FREE plan limits
        private const val FREE_CAPACITY: Long = 20L

        // BASIC plan limits
        private const val BASIC_CAPACITY: Long = 40L

        // PROFESSIONAL plan limits
        private const val PROFESSIONAL_CAPACITY: Long = 100L

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
    }
}
