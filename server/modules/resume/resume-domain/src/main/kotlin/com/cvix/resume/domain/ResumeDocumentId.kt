package com.cvix.resume.domain

import java.util.*

/**
 * Value class representing a unique identifier for a [ResumeDocument].
 *
 * @property value The underlying UUID value.
 */
@JvmInline
value class ResumeDocumentId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        /**
         * Creates a ResumeDocumentId from a string representation of a UUID.
         *
         * @param id The string representation of a UUID.
         * @return A ResumeDocumentId corresponding to given UUID string.
         */
        fun fromString(id: String): ResumeDocumentId = ResumeDocumentId(UUID.fromString(id))
    }
}
