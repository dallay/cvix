package com.cvix.waitlist.domain

/**
 * Recording waitlist-related metrics.
 *
 * This interface defines the contract for metric collection without
 * coupling the application layer to specific metric implementations.
 *
 * Implementations should track:
 * - Waitlist join events with source attribution
 * - Source normalization transformations
 * - Unknown sources for analytics
 */
interface WaitlistMetrics {

    /**
     * Records a waitlist join event with source tracking.
     *
     * @param sourceRaw The raw source string from the client
     * @param sourceNormalized The normalized source enum value
     */
    fun recordWaitlistJoin(sourceRaw: String, sourceNormalized: WaitlistSource)

    /**
     * Records when a source normalization occurs.
     *
     * @param sourceRaw The raw source string
     * @param sourceNormalized The normalized source enum value
     */
    fun recordSourceNormalization(sourceRaw: String, sourceNormalized: WaitlistSource)
}
