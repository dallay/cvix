package com.cvix.common.domain.model

import java.io.Serializable
import java.time.Instant

/**
 * Base class for entities that need to be audited.
 * @property createdAt The date and time when the entity was created.
 * @property updatedAt The date and time when the entity was last updated.
 * @constructor Creates an auditable entity.
 * @param createdAt The date and time when the entity was created.
 * @param createdBy The user or system that created the entity.
 * @param updatedAt The date and time when the entity was last updated.
 * @param updatedBy The user or system that last updated the entity.
 * @see Instant
 * @see Instant.now
 */
abstract class AuditableEntity(
    open val createdAt: Instant = Instant.now(),
    open val createdBy: String = "system",
    open var updatedAt: Instant? = null,
    open var updatedBy: String? = null
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
