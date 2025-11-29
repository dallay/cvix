package com.loomify.ratelimit.infrastructure

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Refill
import java.time.Duration

/**
 * Enum representing different pricing plans for API rate limiting.
 * Each plan defines its own rate limit (Bandwidth).
 */
enum class PricingPlan {
    FREE {
        override fun getLimit(): Bandwidth = Bandwidth.classic(
            FREE_CAPACITY,
            Refill.intervally(FREE_REFILL_TOKENS, Duration.ofHours(REFILL_HOURS)),
        )
    },
    BASIC {
        override fun getLimit(): Bandwidth = Bandwidth.classic(
            BASIC_CAPACITY,
            Refill.intervally(BASIC_REFILL_TOKENS, Duration.ofHours(REFILL_HOURS)),
        )
    },
    PROFESSIONAL {
        override fun getLimit(): Bandwidth = Bandwidth.classic(
            PROFESSIONAL_CAPACITY,
            Refill.intervally(PROFESSIONAL_REFILL_TOKENS, Duration.ofHours(REFILL_HOURS)),
        )
    };

    abstract fun getLimit(): Bandwidth

    companion object {
        // Refill window used by all plans (in hours)
        private const val REFILL_HOURS: Long = 1L

        // FREE plan limits
        private const val FREE_CAPACITY: Long = 20L
        private const val FREE_REFILL_TOKENS: Long = 20L

        // BASIC plan limits
        private const val BASIC_CAPACITY: Long = 40L
        private const val BASIC_REFILL_TOKENS: Long = 40L

        // PROFESSIONAL plan limits
        private const val PROFESSIONAL_CAPACITY: Long = 100L
        private const val PROFESSIONAL_REFILL_TOKENS: Long = 100L

        /**
         * Resolves the pricing plan from a given API key.
         *
         * @param apiKey The API key.
         * @return The corresponding [PricingPlan].
         */
        fun resolvePlanFromApiKey(apiKey: String): PricingPlan {
            return when {
                apiKey.startsWith("PX001-") -> PROFESSIONAL
                apiKey.startsWith("BX001-") -> BASIC
                else -> FREE
            }
        }
    }
}
