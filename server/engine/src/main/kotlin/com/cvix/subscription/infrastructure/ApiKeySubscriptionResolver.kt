package com.cvix.subscription.infrastructure

import com.cvix.subscription.domain.ResolverContext
import com.cvix.subscription.domain.SubscriptionResolver
import com.cvix.subscription.domain.SubscriptionTier
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Implementation of [SubscriptionResolver] that resolves subscription tiers based on API key prefixes.
 *
 * This is the legacy implementation supporting the API key-based plan system.
 * It's now available as a fallback option, configured via SubscriptionResolverConfig.
 *
 * **Supported Context:**
 * - [ResolverContext.ApiKey]: Resolves from API key with format `{PLAN_PREFIX}-{KEY_SUFFIX}`
 *   - `PX001-*`: Professional tier
 *   - `BX001-*`: Basic tier
 *   - Empty or unknown: Free tier
 * - [ResolverContext.UserId]: Not supported; defaults to FREE tier
 *
 * **Note:** The default resolver is now DatabaseSubscriptionResolver.
 * To use this resolver, set: subscription.resolver.type=apikey
 *
 * @since 1.0.0
 * @deprecated Use DatabaseSubscriptionResolver for persistent subscription management
 */
@Deprecated(
    "Use DatabaseSubscriptionResolver instead. This resolver is maintained for " +
        "legacy support only and will be removed in future versions.",
)
@Component
class ApiKeySubscriptionResolver : SubscriptionResolver {
    private val logger = LoggerFactory.getLogger(ApiKeySubscriptionResolver::class.java)

    /**
     * Resolves subscription tier from an API key context.
     *
     * Expects [ResolverContext.ApiKey]. If a [ResolverContext.UserId] is provided,
     * it defaults to FREE tier.
     *
     * @param context The [ResolverContext.ApiKey] to resolve
     * @return The corresponding [SubscriptionTier], defaulting to FREE if not recognized
     */
    override suspend fun resolve(context: ResolverContext): SubscriptionTier {
        val apiKey = when (context) {
            is ResolverContext.ApiKey -> context.key
            is ResolverContext.UserId -> {
                logger.debug("UserId context not supported by ApiKeySubscriptionResolver, resolving to FREE tier")
                return SubscriptionTier.FREE
            }
        }

        if (apiKey.isBlank()) {
            logger.debug("Empty API key provided, resolving to FREE tier")
            return SubscriptionTier.FREE
        }

        val tier = when {
            apiKey.startsWith("PX001-") -> {
                logger.debug(
                    "Resolved PX001 prefix to PROFESSIONAL tier for key: ${
                        apiKey.take(
                            API_KEY_PREVIEW_LENGTH,
                        )
                    }...",
                )
                SubscriptionTier.PROFESSIONAL
            }

            apiKey.startsWith("BX001-") -> {
                logger.debug(
                    "Resolved BX001 prefix to BASIC tier for key: ${
                        apiKey.take(
                            API_KEY_PREVIEW_LENGTH,
                        )
                    }...",
                )
                SubscriptionTier.BASIC
            }

            else -> {
                logger.debug(
                    "Unrecognized API key prefix, resolving to FREE tier for key: ${
                        apiKey.take(
                            API_KEY_PREVIEW_LENGTH,
                        )
                    }...",
                )
                SubscriptionTier.FREE
            }
        }

        return tier
    }

    companion object {
        private const val API_KEY_PREVIEW_LENGTH = 10
    }
}
