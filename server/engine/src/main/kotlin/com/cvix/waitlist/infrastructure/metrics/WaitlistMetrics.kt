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
 *
 * @property meterRegistry Micrometer meter registry for metric registration
 */
@Component
class WaitlistMetrics(private val meterRegistry: MeterRegistry) {

    /**
     * Records a waitlist join event with both raw and normalized source tracking.
     *
     * @param sourceRaw The raw source string from the client
     * @param sourceNormalized The normalized source enum value
     */
    fun recordWaitlistJoin(sourceRaw: String, sourceNormalized: WaitlistSource) {
        // Track overall join with both raw and normalized tags
        meterRegistry.counter(
            "waitlist.join.total",
            "source_raw", sourceRaw,
            "source_normalized", sourceNormalized.value,
        ).increment()

        // Track raw source distribution
        meterRegistry.counter("waitlist.source.raw", "source", sourceRaw).increment()

        // Track normalized source distribution
        meterRegistry.counter("waitlist.source.normalized", "source", sourceNormalized.value).increment()

        // Track when sources are normalized to UNKNOWN (potential new channels)
        if (sourceNormalized == WaitlistSource.UNKNOWN && sourceRaw != "unknown") {
            meterRegistry.counter("waitlist.source.unknown", "raw_source", sourceRaw).increment()
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
        if (sourceRaw == sourceNormalized.value) {
            return // No normalization occurred
        }

        meterRegistry.counter(
            "waitlist.source.normalization",
            "raw", sourceRaw,
            "normalized", sourceNormalized.value,
        ).increment()
    }
}
