package com.loomify.resume.domain

import com.loomify.common.domain.vo.email.Email
import java.net.URI
import java.util.Locale

/**
 * FullName value object with validation.
 * - Required, cannot be blank
 * - Max 100 characters
 */
@JvmInline
value class FullName(val value: String) {
    init {
        require(value.isNotBlank()) { "Full name cannot be blank" }
        require(value.length <= MAX_FULLNAME_LENGTH) { "Full name cannot exceed $MAX_FULLNAME_LENGTH characters" }
    }

    companion object {
        private const val MAX_FULLNAME_LENGTH = 100
    }
}

/**
 * JobTitle value object with validation.
 * - Required, max 100 characters
 */
@JvmInline
value class JobTitle(val value: String) {
    init {
        require(value.isNotBlank()) { "Job title cannot be blank" }
        require(value.length <= MAX_JOB_TITLE_LENGTH) { "Job title cannot exceed $MAX_JOB_TITLE_LENGTH characters" }
    }

    companion object {
        private const val MAX_JOB_TITLE_LENGTH = 200
    }
}

/**
 * PhoneNumber value object.
 * Accepts various formats (international, local, formatted).
 */
@JvmInline
value class PhoneNumber(val value: String) {
    init {
        require(value.isNotBlank()) { "Phone number cannot be blank" }
    }
}

/**
 * Url value object with RFC-compliant validation.
 */
@JvmInline
value class Url private constructor(val value: String) {
    override fun toString(): String = value

    companion object {
        private val VALID_SCHEMES = setOf("http", "https")

        operator fun invoke(value: String): Url {
            require(value.isNotBlank()) { "URL cannot be blank" }

            val normalized = value.trim()
            val uri = runCatching { URI(normalized) }
                .getOrElse { throw IllegalArgumentException("Invalid URL format: ${it.message}") }

            val scheme = uri.scheme?.lowercase(Locale.ROOT)
            require(scheme in VALID_SCHEMES) {
                "URL scheme must be http or https, got: ${uri.scheme}"
            }
            require(!uri.host.isNullOrBlank()) {
                "URL must contain a valid host"
            }

            return Url(normalized)
        }
    }
}

/**
 * Summary value object with validation.
 * - Max 500 characters
 */
@JvmInline
value class Summary(val value: String) {
    init {
        require(value.length <= MAX_SUMMARY_LENGTH) { "Summary cannot exceed $MAX_SUMMARY_LENGTH characters" }
    }

    companion object {
        private const val MAX_SUMMARY_LENGTH = 600
    }
}

/**
 * Location data class representing physical address.
 */
data class Location(
    val address: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    val countryCode: String? = null,
    val region: String? = null,
)

/**
 * SocialProfile data class for social media links.
 */
data class SocialProfile(
    val network: String,
    val username: String,
    val url: String,
)

/**
 * Basics value object representing the basics section of a resume.
 * Contains all personal information fields per JSON Resume Schema.
 * Aligned with JSON Resume specification: https://jsonresume.org/schema
 */
data class Basics(
    val name: FullName,
    val label: JobTitle? = null,
    val image: Url? = null,
    val email: Email,
    val phone: PhoneNumber? = null,
    val url: Url? = null,
    val summary: Summary? = null,
    val location: Location? = null,
    val profiles: List<SocialProfile> = emptyList(),
)
