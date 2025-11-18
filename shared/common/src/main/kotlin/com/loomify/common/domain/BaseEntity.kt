package com.loomify.common.domain

import com.loomify.common.domain.bus.event.DomainEvent
import java.io.Serializable
import java.time.Instant

/**
 * Abstract base class for all domain entities.
 * It provides common properties for identification, auditing, and domain event management.
 *
 * @param ID The type of the unique identifier for the entity.
 */
abstract class BaseEntity<ID> : Serializable {
    abstract val id: ID
    open val createdAt: Instant = Instant.now()
    open val createdBy: String = "system"
    open val updatedAt: Instant? = null
    open val updatedBy: String? = null
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    /**
     * Records a domain event associated with this entity.
     *
     * @param event The [DomainEvent] to record.
     */
    fun record(event: DomainEvent) = domainEvents.add(event)

    /**
     * Retrieves all recorded domain events and clears the internal list.
     * This is typically called by the persistence layer to dispatch events.
     *
     * @return A list of [DomainEvent]s.
     */
    fun pullDomainEvents(): List<DomainEvent> {
        val events = domainEvents.toList()
        clearDomainEvents()
        return events
    }

    /**
     * Clears all domain events from the internal list.
     */
    private fun clearDomainEvents() = domainEvents.clear()

    @Generated
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseEntity<*>) return false

        if (id != other.id) return false
        if (domainEvents != other.domainEvents) return false

        return true
    }

    @Generated
    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + domainEvents.hashCode()
        return result
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
