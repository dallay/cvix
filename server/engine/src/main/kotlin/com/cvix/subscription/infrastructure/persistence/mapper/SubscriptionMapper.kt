package com.cvix.subscription.infrastructure.persistence.mapper

import com.cvix.subscription.domain.Subscription
import com.cvix.subscription.domain.SubscriptionId
import com.cvix.subscription.domain.SubscriptionStatus
import com.cvix.subscription.domain.SubscriptionTier
import com.cvix.subscription.infrastructure.persistence.entity.SubscriptionEntity

/**
 * Mapper functions to convert between Subscription domain model and SubscriptionEntity.
 *
 * @created 12/11/25
 */
object SubscriptionMapper {

    /**
     * Converts a SubscriptionEntity to a Subscription domain model.
     */
    fun SubscriptionEntity.toDomain(): Subscription = Subscription(
        id = SubscriptionId(id),
        userId = userId,
        tier = SubscriptionTier.valueOf(tier),
        status = SubscriptionStatus.valueOf(status),
        validFrom = validFrom,
        validUntil = validUntil,
    )

    /**
     * Converts a Subscription domain model to a SubscriptionEntity.
     */
    fun Subscription.toEntity(): SubscriptionEntity = SubscriptionEntity(
        id = id.id,
        userId = userId,
        tier = tier.name,
        status = status.name,
        validFrom = validFrom,
        validUntil = validUntil,
    )
}
