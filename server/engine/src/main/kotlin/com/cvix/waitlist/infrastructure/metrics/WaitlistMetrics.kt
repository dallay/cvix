package com.cvix.waitlist.infrastructure.metrics

import com.cvix.waitlist.domain.WaitlistSource
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

/**
 * Metrics collector for waitlist operations.
 *
 * Tracks both raw and normalized source values to enable:
 * - Discovery of new marketing channels without code changes
 * - Aggregated analytics by normalized source
 * - Granular analytics by raw source value
 *
 * Metrics exposed:
 * - waitlist.join.total: Counter for all waitlist joins (tags: source_raw, source_normalized)
 * - waitlist.source.raw: Counter for raw sources (tag: source)
 * - waitlist.source.normalized: Counter for normalized sources (tag: source)
 * - waitlist.source.unknown: Counter for unknown sources that were normalized
 * - waitlist.source.normalization: Counter for source transformations (tags: raw, normalized)
 *
 * **Cardinality Mitigation:**
 * Raw source values are sanitized to prevent unbounded metric cardinality. Invalid values
 * (excessive length, invalid characters) are replaced with "invalid" to avoid memory exhaustion
 * from malicious or malformed inputs.
 *
 * @property meterRegistry Micrometer meter registry for metric registration
 */
@Component
class WaitlistMetrics(private val meterRegistry: MeterRegistry) {

    /**
     * Sanitizes a raw source value to prevent unbounded metric cardinality.
     * Returns "invalid" for values that are empty, too long (>100 chars), or contain invalid characters.
     *
     * @param sourceRaw The raw source string to sanitize
     * @return Sanitized source string safe for use as a metric tag
     */
    private fun sanitizeSourceForMetrics(sourceRaw: String): String {
        return if (sourceRaw.length > MAX_SOURCE_LENGTH || !sourceRaw.matches(VALID_SOURCE_PATTERN)) {
            INVALID_SOURCE_TAG
        } else {
            sourceRaw
        }
    }

    /**
     * Records a waitlist join event with both raw and normalized source tracking.
     *
     * @param sourceRaw The raw source string from the client
     * @param sourceNormalized The normalized source enum value
     */
    fun recordWaitlistJoin(sourceRaw: String, sourceNormalized: WaitlistSource) {
        val sanitizedRaw = sanitizeSourceForMetrics(sourceRaw)

        // Track overall join with both raw and normalized tags
        meterRegistry.counter(
            "waitlist.join.total",
            "source_raw", sanitizedRaw,
            "source_normalized", sourceNormalized.value,
        ).increment()

        // Track raw source distribution
        meterRegistry.counter("waitlist.source.raw", "source", sanitizedRaw).increment()

        // Track normalized source distribution
        meterRegistry.counter("waitlist.source.normalized", "source", sourceNormalized.value)
            .increment()

        // Track when sources are normalized to UNKNOWN (potential new channels)
        if (sourceNormalized == WaitlistSource.UNKNOWN &&
            !sanitizedRaw.equals("unknown", ignoreCase = true)
        ) {
            meterRegistry.counter("waitlist.source.unknown", "raw_source", sanitizedRaw).increment()
        }
    }

    /**
     * Records when a source normalization occurs (raw differs from normalized).
     *
     * This tracks any transformation from raw to normalized, including:
     * - Case changes: "Landing-CTA" → "landing-cta"
     * - Unknown sources: "twitter-new-campaign" → "unknown"
     *
     * @param sourceRaw The raw source string
     * @param sourceNormalized The normalized source enum value
     */
    fun recordSourceNormalization(sourceRaw: String, sourceNormalized: WaitlistSource) {
        val sanitizedRaw = sanitizeSourceForMetrics(sourceRaw)

        if (sanitizedRaw == sourceNormalized.value) {
            return // No normalization occurred
        }

        meterRegistry.counter(
            "waitlist.source.normalization",
            "raw", sanitizedRaw,
            "normalized", sourceNormalized.value,
        ).increment()
    }

    companion object {
        private const val MAX_SOURCE_LENGTH = 100
        private val VALID_SOURCE_PATTERN = Regex("^[a-zA-Z0-9_-]+$")
        private const val INVALID_SOURCE_TAG = "invalid"
    }
}
