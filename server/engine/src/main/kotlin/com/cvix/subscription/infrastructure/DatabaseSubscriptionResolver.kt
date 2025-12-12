package com.cvix.subscription.infrastructure

import com.cvix.subscription.domain.SubscriptionRepository
import com.cvix.subscription.domain.SubscriptionResolver
import com.cvix.subscription.domain.SubscriptionTier
import java.util.*
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
 * This approach provides:
 * - Persistent subscription management
 * - Support for subscription validity periods
 * - Subscription status tracking (ACTIVE, CANCELLED, EXPIRED)
 * - Flexibility to integrate with payment providers (Stripe, etc.)
 *
 * @param subscriptionRepository The repository for querying subscriptions
 * @created 12/11/25
 */
@Component
@Primary
class DatabaseSubscriptionResolver(
    private val subscriptionRepository: SubscriptionRepository
) : SubscriptionResolver {
    private val logger = LoggerFactory.getLogger(DatabaseSubscriptionResolver::class.java)

    /**
     * Resolves subscription tier from the database for the given user ID.
     *
     * The context parameter is expected to be a user ID (UUID string).
     *
     * @param context The user ID as a string
     * @return The user's current [SubscriptionTier], defaulting to FREE if not found
     */
    override suspend fun resolve(context: String): SubscriptionTier {
        if (context.isBlank()) {
            logger.debug("Empty user ID provided, resolving to FREE tier")
            return SubscriptionTier.FREE
        }

        return try {
            val userId = UUID.fromString(context)
            val subscription = subscriptionRepository.findActiveByUserId(userId)

            if (subscription != null) {
                logger.debug(
                    "Resolved subscription tier {} for user {} (subscription: {})",
                    subscription.tier,
                    userId,
                    subscription.id,
                )
                subscription.tier
            } else {
                logger.debug("No active subscription found for user {}, resolving to FREE tier", userId)
                SubscriptionTier.FREE
            }
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid user ID format: {}, resolving to FREE tier", context, e)
            SubscriptionTier.FREE
        } catch (@Suppress("TooGenericExceptionCaught")e: Exception) {
            logger.error("Error resolving subscription for user: {}, resolving to FREE tier", context, e)
            SubscriptionTier.FREE
        }
    }
}
