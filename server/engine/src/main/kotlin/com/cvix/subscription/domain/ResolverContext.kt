package com.cvix.subscription.domain

import java.util.UUID

/**
 * Sealed interface representing the context used to resolve a user's subscription tier.
 *
 * This provides type-safe context variants for different resolution strategies:
 * - [ApiKey]: Resolves subscription tier from an API key (legacy support)
 * - [UserId]: Resolves subscription tier from a user identifier (primary approach)
 *
 * Using a sealed interface ensures type safety at compile time and prevents passing
 * incorrect context types to implementations. Each implementation of [SubscriptionResolver]
 * documents which context variant(s) it expects.
 *
 * @since 1.0.0
 */
sealed interface ResolverContext {
    /**
     * Context representing an API key-based subscription resolution.
     *
     * Expected format: `{PLAN_PREFIX}-{KEY_SUFFIX}`
     * Examples:
     * - `PX001-ABC123XYZ`: Professional tier
     * - `BX001-ABC123XYZ`: Basic tier
     *
     * @param key The API key string
     */
    data class ApiKey(val key: String) : ResolverContext

    /**
     * Context representing a user ID-based subscription resolution.
     *
     * Expected format: UUID (RFC 4122)
     * Example: `123e4567-e89b-12d3-a456-426614174000`
     *
     * @param userId The user identifier as a UUID
     */
    data class UserId(val userId: UUID) : ResolverContext
}
