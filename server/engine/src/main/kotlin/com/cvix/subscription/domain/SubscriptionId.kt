package com.cvix.subscription.domain

import com.cvix.common.domain.BaseId
import java.util.UUID

/**
 * Value object representing a unique subscription identifier.
 *
 * @param id The UUID value for this subscription identifier
 * @created 12/11/25
 */
data class SubscriptionId(override val id: UUID) : BaseId<UUID>(id) {
    constructor(id: String) : this(UUID.fromString(id))

    companion object {
        fun random(): SubscriptionId = SubscriptionId(UUID.randomUUID())
    }
}
