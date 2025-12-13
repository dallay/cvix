package com.cvix.subscription.domain

/**
 * Port interface for resolving a user's subscription tier.
 *
 * This interface allows the application and domain layers to remain decoupled
 * from the infrastructure details of how subscriptions are resolved.
 *
 * Implementations can:
 * - Resolve from API key prefixes (current approach)
 * - Query a database for subscription records
 * - Call an external service (Stripe, etc.)
 * - Use in-memory configuration
 *
 * This abstraction makes it easy to change the subscription resolution mechanism
 * without modifying domain or application logic.
 *
 * @created 11/12/25
 */
fun interface SubscriptionResolver {
    /**
     * Resolves the subscription tier for a given context.
     *
     * Context can be:
     * - An API key string
     * - A user ID
     * - Any other identifier the implementation understands
     *
     * @param context The context to resolve the subscription tier from
     * @return The user's [SubscriptionTier], defaulting to FREE if not found
     */
    suspend fun resolve(context: String): SubscriptionTier
}
