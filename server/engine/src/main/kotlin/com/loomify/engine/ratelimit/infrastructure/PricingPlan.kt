package com.loomify.engine.ratelimit.infrastructure

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Refill
import java.time.Duration

/**
 * Enum representing different pricing plans for API rate limiting.
 * Each plan defines its own rate limit (Bandwidth).
 */
enum class PricingPlan {
    FREE {
        override fun getLimit(): Bandwidth {
            return Bandwidth.classic(20, Refill.intervally(20, Duration.ofHours(1)))
        }
    },
    BASIC {
        override fun getLimit(): Bandwidth {
            return Bandwidth.classic(40, Refill.intervally(40, Duration.ofHours(1)))
        }
    },
    PROFESSIONAL {
        override fun getLimit(): Bandwidth {
            return Bandwidth.classic(100, Refill.intervally(100, Duration.ofHours(1)))
        }
    };

    abstract fun getLimit(): Bandwidth

    companion object {
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
