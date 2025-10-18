package com.loomify.common.domain

import java.io.Serializable
import java.time.LocalDateTime

/**
 * Base class for entities that need to be audited.
 * @property createdAt The date and time when the entity was created.
 * @property updatedAt The date and time when the entity was last updated.
 * @constructor Creates an auditable entity.
 * @param createdAt The date and time when the entity was created.
 * @param createdBy The user or system that created the entity.
 * @param updatedAt The date and time when the entity was last updated.
 * @param updatedBy The user or system that last updated the entity.
 * @see LocalDateTime
 * @see LocalDateTime.now
 */
abstract class AuditableEntity(
    open val createdAt: LocalDateTime = LocalDateTime.now(),
    open val createdBy: String = "system",
    open var updatedAt: LocalDateTime? = null,
    open var updatedBy: String? = null
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
