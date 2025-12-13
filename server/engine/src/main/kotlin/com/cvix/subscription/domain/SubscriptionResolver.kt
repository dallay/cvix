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
 * Implementations must specify which [ResolverContext] variant(s) they support
 * in their documentation.
 *
 * @since 1.0.0
 */
fun interface SubscriptionResolver {
    /**
     * Resolves the subscription tier for a given context.
     *
     * The exact behavior depends on the [ResolverContext] variant and the
     * implementation. Refer to specific implementation documentation for
     * expected context format and resolution logic.
     *
     * @param context The [ResolverContext] to resolve the subscription tier from
     * @return The user's [SubscriptionTier], defaulting to FREE if not found or if context is invalid
     */
    suspend fun resolve(context: ResolverContext): SubscriptionTier
}
