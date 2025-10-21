package com.loomify.engine.authentication.infrastructure.ratelimit

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Scheduled task for cleaning up expired rate limit entries.
 *
 * Runs every 5 minutes to prevent memory leaks from accumulating
 * expired rate limit data.
 *
 * @property rateLimiter The rate limiter to clean up
 * @since 1.0.0
 */
@Component
class RateLimiterCleanupTask(
    private val rateLimiter: InMemoryRateLimiter
) {

    private val logger = LoggerFactory.getLogger(RateLimiterCleanupTask::class.java)

    /**
     * Cleans up expired rate limit entries.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    fun cleanupExpiredEntries() {
        logger.debug("Running rate limiter cleanup task")

        // Use the longest window duration (1 hour) to ensure we don't prematurely clean up entries
        rateLimiter.cleanup(Duration.ofHours(1))
    }
}
