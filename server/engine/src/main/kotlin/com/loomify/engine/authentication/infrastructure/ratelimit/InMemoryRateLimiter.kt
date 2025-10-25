package com.loomify.engine.authentication.infrastructure.ratelimit

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * In-memory rate limiter using a sliding window algorithm.
 *
 * This implementation tracks request counts per identifier (e.g., IP address, email)
 * within a time window. It uses a sliding window approach to provide more accurate
 * rate limiting compared to fixed windows.
 *
 * **Note**: This is an in-memory implementation suitable for single-instance deployments.
 * For distributed deployments, consider using Redis-based rate limiting.
 *
 * **Requirements**: FR-010 (rate limiting for login), FR-004 (rate limiting for registration)
 *
 * @since 1.0.0
 */
@Component
open class InMemoryRateLimiter {
    private val logger = LoggerFactory.getLogger(InMemoryRateLimiter::class.java)

    // Store: identifier -> list of timestamps
    private val requestMap = ConcurrentHashMap<String, MutableList<Instant>>()

    /**
     * Checks if a request should be allowed based on rate limits.
     *
     * @param identifier The identifier to rate limit (e.g., IP address, email)
     * @param maxAttempts Maximum number of attempts allowed in the time window
     * @param windowDuration Duration of the time window
     * @return true if the request is allowed, false if rate limit exceeded
     */
    open fun isAllowed(
        identifier: String,
        maxAttempts: Int,
        windowDuration: Duration
    ): Boolean {
        val now = Instant.now()
        val windowStart = now.minus(windowDuration)

        // Get or create the request list for this identifier
        val requests = requestMap.computeIfAbsent(identifier) { mutableListOf() }

        synchronized(requests) {
            // Remove expired requests outside the window
            requests.removeIf { it.isBefore(windowStart) }

            // Check if under the limit
            if (requests.size < maxAttempts) {
                requests.add(now)
                logger.debug("Rate limit check passed for identifier: $identifier (${requests.size}/$maxAttempts)")
                return true
            }

            logger.warn("Rate limit exceeded for identifier: $identifier (${requests.size}/$maxAttempts)")
            return false
        }
    }

    /**
     * Gets the number of remaining attempts for an identifier.
     *
     * @param identifier The identifier to check
     * @param maxAttempts Maximum number of attempts allowed
     * @param windowDuration Duration of the time window
     * @return The number of remaining attempts
     */
    open fun getRemainingAttempts(
        identifier: String,
        maxAttempts: Int,
        windowDuration: Duration
    ): Int {
        val now = Instant.now()
        val windowStart = now.minus(windowDuration)

        val requests = requestMap[identifier] ?: return maxAttempts

        synchronized(requests) {
            requests.removeIf { it.isBefore(windowStart) }
            return maxOf(0, maxAttempts - requests.size)
        }
    }

    /**
     * Gets the time until the rate limit resets for an identifier.
     *
     * This method calculates when the oldest request in the current window will expire,
     * allowing a new request to be made. Returns null if the identifier is not currently
     * rate-limited.
     *
     * @param identifier The identifier to check
     * @param maxAttempts Maximum number of attempts allowed in the time window
     * @param windowDuration Duration of the time window
     * @return Duration until reset, or null if not currently rate-limited
     */
    open fun getTimeUntilReset(
        identifier: String,
        maxAttempts: Int,
        windowDuration: Duration
    ): Duration? {
        val now = Instant.now()
        val windowStart = now.minus(windowDuration)

        val requests = requestMap[identifier] ?: return null

        synchronized(requests) {
            requests.removeIf { it.isBefore(windowStart) }

            if (requests.size < maxAttempts) return null

            val oldestRequest = requests.firstOrNull() ?: return null
            val resetTime = oldestRequest.plus(windowDuration)

            return if (resetTime.isAfter(now)) {
                Duration.between(now, resetTime)
            } else {
                null
            }
        }
    }

    /**
     * Clears rate limiting data for an identifier.
     * Useful for testing or manual reset by administrators.
     *
     * @param identifier The identifier to clear
     */
    fun clear(identifier: String) {
        requestMap.remove(identifier)
        logger.info("Cleared rate limit data for identifier: $identifier")
    }

    /**
     * Clears all rate limiting data.
     * Useful for testing or system maintenance.
     */
    fun clearAll() {
        requestMap.clear()
        logger.info("Cleared all rate limit data")
    }

    /**
     * Performs cleanup of expired entries to prevent memory leaks.
     * Should be called periodically (e.g., via scheduled task).
     */
    fun cleanup(windowDuration: Duration) {
        val now = Instant.now()
        val windowStart = now.minus(windowDuration)
        var cleanedCount = 0

        requestMap.entries.removeIf { (identifier, requests) ->
            synchronized(requests) {
                requests.removeIf { it.isBefore(windowStart) }
                if (requests.isEmpty()) {
                    cleanedCount++
                    true
                } else {
                    false
                }
            }
        }

        if (cleanedCount > 0) {
            logger.debug("Cleaned up $cleanedCount expired rate limit entries")
        }
    }
}
