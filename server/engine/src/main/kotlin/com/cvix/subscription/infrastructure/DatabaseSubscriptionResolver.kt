package com.cvix.subscription.infrastructure

import com.cvix.subscription.domain.ResolverContext
import com.cvix.subscription.domain.SubscriptionRepository
import com.cvix.subscription.domain.SubscriptionResolver
import com.cvix.subscription.domain.SubscriptionTier
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

/**
 * Implementation of [SubscriptionResolver] that resolves subscription tiers from the database.
 *
 * This resolver queries the subscriptions table to find the active subscription
 * for a user and returns their current tier. If no active subscription is found,
 * it defaults to the FREE tier.
 *
 * **Supported Context:**
 * - [ResolverContext.UserId]: Resolves from database using the provided UUID
 * - [ResolverContext.ApiKey]: Not supported; defaults to FREE tier
 *
 * This approach provides:
 * - Persistent subscription management
 * - Support for subscription validity periods
 * - Subscription status tracking (ACTIVE, CANCELLED, EXPIRED)
 * - Flexibility to integrate with payment providers (Stripe, etc.)
 *
 * @param subscriptionRepository The repository for querying subscriptions
 * @since 1.0.0
 */
@Component
@Primary
class DatabaseSubscriptionResolver(
    private val subscriptionRepository: SubscriptionRepository
) : SubscriptionResolver {
    private val log = LoggerFactory.getLogger(DatabaseSubscriptionResolver::class.java)

    /**
     * Resolves subscription tier from the database for the given user ID context.
     *
     * Expects [ResolverContext.UserId]. If a [ResolverContext.ApiKey] is provided,
     * it defaults to FREE tier.
     *
     * @param context The [ResolverContext.UserId] containing the user identifier
     * @return The user's current [SubscriptionTier], defaulting to FREE if not found
     */
    override suspend fun resolve(context: ResolverContext): SubscriptionTier {
        val userId = when (context) {
            is ResolverContext.UserId -> context.userId
            is ResolverContext.ApiKey -> {
                log.debug("ApiKey context not supported by DatabaseSubscriptionResolver, resolving to FREE tier")
                return SubscriptionTier.FREE
            }
        }

        return try {
            val subscription = subscriptionRepository.findActiveByUserId(userId)

            if (subscription != null) {
                log.debug(
                    "Resolved subscription tier {} for user {} (subscription: {})",
                    subscription.tier,
                    userId,
                    subscription.id,
                )
                subscription.tier
            } else {
                log.debug("No active subscription found for user {}, resolving to FREE tier", userId)
                SubscriptionTier.FREE
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            log.error("Error resolving subscription for user: {}, resolving to FREE tier", userId, e)
            SubscriptionTier.FREE
        }
    }
}
