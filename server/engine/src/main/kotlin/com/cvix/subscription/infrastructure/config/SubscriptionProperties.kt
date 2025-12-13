package com.cvix.subscription.infrastructure.config

import com.cvix.subscription.domain.SubscriptionTier
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * Spring Boot configuration properties for subscription plan settings.
 *
 * Allows externalization of plan configurations to application.yml for runtime adjustment.
 *
 * Example YAML configuration:
 * ```yaml
 * subscription:
 *   plans:
 *     free:
 *       rate-limit-capacity: 20
 *       premium-templates-enabled: false
 *       max-premium-templates: 0
 *       priority: false
 *     basic:
 *       rate-limit-capacity: 40
 *       premium-templates-enabled: true
 *       max-premium-templates: 5
 *       priority: false
 *     professional:
 *       rate-limit-capacity: 100
 *       premium-templates-enabled: true
 *       max-premium-templates: 100
 *       priority: true
 * ```
 *
 * @created 11/12/25
 */
@Validated
@ConfigurationProperties(prefix = "subscription")
data class SubscriptionProperties(
    /**
     * Configuration for subscription plans.
     */
    val plans: PlansConfig = PlansConfig()
) {
    /**
     * Contains configuration for each subscription tier.
     */
    data class PlansConfig(
        val free: TierConfig = TierConfig(
            rateLimitCapacity = 20,
            premiumTemplatesEnabled = false,
            maxPremiumTemplates = 0,
            priority = false,
        ),
        val basic: TierConfig = TierConfig(
            rateLimitCapacity = 40,
            premiumTemplatesEnabled = true,
            maxPremiumTemplates = 5,
            priority = false,
        ),
        val professional: TierConfig = TierConfig(
            rateLimitCapacity = 100,
            premiumTemplatesEnabled = true,
            maxPremiumTemplates = 100,
            priority = true,
        )
    )

    /**
     * Configuration for an individual subscription tier.
     *
     * @param rateLimitCapacity Rate limit capacity (requests per hour)
     * @param premiumTemplatesEnabled Whether this tier can access premium templates
     * @param maxPremiumTemplates Maximum number of premium templates this tier can access
     * @param priority Whether this tier gets priority processing
     * @param customFeatures Optional map of custom feature flags
     */
    data class TierConfig(
        @field:Min(1)
        val rateLimitCapacity: Long = 20,
        val premiumTemplatesEnabled: Boolean = false,
        @field:Min(0)
        val maxPremiumTemplates: Int = 0,
        val priority: Boolean = false,
        val customFeatures: Map<String, Boolean> = emptyMap()
    )

    /**
     * Converts properties to a map of tier configurations for easy lookup.
     */
    fun toTierConfigMap(): Map<SubscriptionTier, TierConfig> = mapOf(
        SubscriptionTier.FREE to plans.free,
        SubscriptionTier.BASIC to plans.basic,
        SubscriptionTier.PROFESSIONAL to plans.professional,
    )
}
