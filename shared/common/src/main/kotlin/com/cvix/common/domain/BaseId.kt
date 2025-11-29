package com.cvix.common.domain

/**
 * Represents a base class for creating ID objects.
 *
 * @param id The value of the ID.
 * @param T The type of the ID value.
 *
 */
@Suppress("unused")
abstract class BaseId<T> protected constructor(open val id: T) {

    @Generated
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseId<*>) return false

        return id == other.id
    }

    @Generated
    override fun hashCode(): Int = id?.hashCode() ?: 0
    @Generated
    override fun toString(): String = id.toString()
}
