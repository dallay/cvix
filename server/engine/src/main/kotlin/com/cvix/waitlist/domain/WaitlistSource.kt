package com.cvix.waitlist.domain

/**
 * Enum representing the source from where a user joined the waitlist.
 *
 * @property value The string representation of the source.
 */
enum class WaitlistSource(val value: String) {
    LANDING_HERO("landing-hero"),
    LANDING_CTA("landing-cta"),
    BLOG_CTA("blog-cta"),
    UNKNOWN("unknown");

    companion object {
        fun fromString(value: String): WaitlistSource =
            entries.find { it.value.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}
