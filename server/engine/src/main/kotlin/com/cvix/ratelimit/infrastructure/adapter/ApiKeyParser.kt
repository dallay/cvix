package com.cvix.ratelimit.infrastructure.adapter

import com.cvix.ratelimit.infrastructure.config.RateLimitProperties
import com.cvix.subscription.domain.SubscriptionTier
import org.springframework.stereotype.Component

/**
 * Component responsible for parsing API keys and extracting subscription tier information.
 *
 * This component externalizes the API key prefix detection logic from the rate limiter,
 * making the system more maintainable and following the Open/Closed Principle.
 * New tier prefixes can be added through configuration without code changes.
 *
 * @property properties Rate limit configuration containing API key prefix mappings
 */
@Component
class ApiKeyParser(
    private val properties: RateLimitProperties
) {

    /**
     * Extracts the subscription tier from an API key based on its prefix.
     *
     * The prefix-to-tier mapping is configured in [RateLimitProperties.apiKeyPrefixes].
     * This allows for flexible tier detection without hardcoding prefix values.
     *
     * **Matching Strategy:**
     * - Checks if the API key starts with the configured professional prefix
     * - If not, checks if it starts with the configured basic prefix
     * - If neither match, defaults to FREE tier
     *
     * **Examples** (with default prefixes):
     * - `"PX001-abc123"` → [SubscriptionTier.PROFESSIONAL]
     * - `"BX001-xyz789"` → [SubscriptionTier.BASIC]
     * - `"unknown-key"` → [SubscriptionTier.FREE]
     * - `""` → [SubscriptionTier.FREE]
     *
     * @param apiKey The API key to parse. Must not be null.
     * @return The extracted [SubscriptionTier]. Never returns null; defaults to FREE.
     */
    fun extractTier(apiKey: String): SubscriptionTier {
        val prefixes = properties.apiKeyPrefixes

        return when {
            apiKey.startsWith(prefixes.professional) -> SubscriptionTier.PROFESSIONAL
            apiKey.startsWith(prefixes.basic) -> SubscriptionTier.BASIC
            else -> SubscriptionTier.FREE
        }
    }

    /**
     * Extracts the subscription tier name (lowercase) from an API key.
     *
     * This is a convenience method that delegates to [extractTier] and converts
     * the result to lowercase for use in configuration lookups.
     *
     * **Examples** (with default prefixes):
     * - `"PX001-abc123"` → `"professional"`
     * - `"BX001-xyz789"` → `"basic"`
     * - `"unknown-key"` → `"free"`
     *
     * @param apiKey The API key to parse. Must not be null.
     * @return The tier name in lowercase (e.g., "free", "basic", "professional").
     */
    fun extractTierName(apiKey: String): String = extractTier(apiKey).name.lowercase()
}
