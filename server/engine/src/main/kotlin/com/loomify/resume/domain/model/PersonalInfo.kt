package com.loomify.resume.domain.model

import com.loomify.common.domain.vo.email.Email

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
        require(value.length <= MAX_JOBTITLE_LENGTH) { "Job title cannot exceed $MAX_JOBTITLE_LENGTH characters" }
    }
    companion object {
        private const val MAX_JOBTITLE_LENGTH = 100
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
 * Url value object with basic validation.
 */
@JvmInline
value class Url(val value: String) {
    init {
        require(value.isNotBlank()) { "URL cannot be blank" }
        require(value.startsWith("http://") || value.startsWith("https://")) {
            "URL must start with http:// or https://"
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
        private const val MAX_SUMMARY_LENGTH = 500
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
 * PersonalInfo value object representing the basics section of a resume.
 * Contains all personal information fields per JSON Resume Schema.
 */
data class PersonalInfo(
    val fullName: FullName,
    val label: JobTitle? = null,
    val image: Url? = null,
    val email: Email,
    val phone: PhoneNumber? = null,
    val url: Url? = null,
    val summary: Summary? = null,
    val location: Location? = null,
    val profiles: List<SocialProfile> = emptyList(),
)
