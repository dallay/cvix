package com.loomify.engine.authentication.infrastructure.ratelimit

import java.time.Duration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

/**
 * Test configuration that provides a permissive rate limiter for integration tests.
 * This prevents rate limiting from interfering with test execution.
 */
@TestConfiguration
class TestRateLimiterConfiguration {

    /**
     * Provides a test rate limiter that always allows requests.
     * This bean is marked as @Primary so it will override the production InMemoryRateLimiter.
     */
    @Bean
    @Primary
    fun inMemoryRateLimiter(): InMemoryRateLimiter = PermissiveRateLimiter()
}

/**
 * A rate limiter implementation that always allows all requests.
 * Used exclusively for testing purposes.
 */
class PermissiveRateLimiter : InMemoryRateLimiter() {
    /**
     * Always allows requests in tests.
     */
    override fun isAllowed(
        identifier: String,
        maxAttempts: Int,
        windowDuration: Duration
    ): Boolean = true

    /**
     * Always returns the maximum number of attempts.
     */
    override fun getRemainingAttempts(
        identifier: String,
        maxAttempts: Int,
        windowDuration: Duration
    ): Int = maxAttempts
}
