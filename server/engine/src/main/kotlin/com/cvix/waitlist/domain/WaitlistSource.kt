package com.cvix.waitlist.domain

import org.slf4j.LoggerFactory

/**
 * Enum representing the source from where a user joined the waitlist.
 *
 * This enum supports extensibility by accepting unknown source values and normalizing
 * them to [UNKNOWN]. When an unrecognized source is received, it is logged for monitoring
 * and analytics purposes, allowing teams to discover new marketing channels without
 * requiring code changes or redeployment.
 *
 * @property value The string representation of the source.
 */
enum class WaitlistSource(val value: String) {
    LANDING_HERO("landing-hero"),
    LANDING_CTA("landing-cta"),
    BLOG_CTA("blog-cta"),
    UNKNOWN("unknown");

    companion object {
        private val logger = LoggerFactory.getLogger(WaitlistSource::class.java)

        /**
         * Parses a raw source string and returns the corresponding enum value.
         *
         * If the source is not recognized, it returns [UNKNOWN] and logs the raw value
         * for observability. This enables future growth without requiring backend changes.
         *
         * @param value The raw source string from the client.
         * @return The corresponding [WaitlistSource] or [UNKNOWN] if not recognized.
         */
        fun fromString(value: String): WaitlistSource {
            val normalized = entries.find { it.value.equals(value, ignoreCase = true) }

            if (normalized == null) {
                logger.warn(
                    "Unknown waitlist source received and normalized to 'unknown': raw_source='{}'",
                    value,
                )
            }

            return normalized ?: UNKNOWN
        }
    }
}
