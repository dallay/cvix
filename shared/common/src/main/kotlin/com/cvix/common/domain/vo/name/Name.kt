package com.cvix.common.domain.vo.name

/**
 * Represents a person's name, consisting of a first name and an optional last name.
 * Provides functionality to retrieve the full name and compare names lexicographically.
 *
 * @property firstName The user's first name, encapsulated in a [FirstName] value object.
 * @property lastName The user's optional last name, encapsulated in a [LastName] value object.
 */
data class Name(val firstName: FirstName, val lastName: LastName?) : Comparable<Name> {

    /**
     * Returns the full name of the user (first name + last name)
     * @return the full name of the user
     */
    fun fullName(): String = "${firstName.value} ${lastName?.value ?: ""}".trim()

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override operator fun compareTo(other: Name): Int = fullName().compareTo(other.fullName())

    override fun toString(): String = fullName()

    companion object {
        fun of(firstName: String, lastName: String?): Name =
            Name(FirstName(firstName), lastName?.let { LastName(it) })
    }
}
