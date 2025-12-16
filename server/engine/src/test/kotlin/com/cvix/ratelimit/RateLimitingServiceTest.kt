package com.cvix.ratelimit

import com.cvix.ratelimit.application.RateLimitingService
import com.cvix.ratelimit.domain.RateLimitResult
import com.cvix.ratelimit.domain.RateLimitStrategy
import com.cvix.ratelimit.domain.RateLimiter
import com.cvix.ratelimit.domain.event.RateLimitExceededEvent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Unit tests for RateLimitingService.
 *
 * Tests cover:
 * - Token consumption with default and specific strategies
 * - Event publishing when rate limit is exceeded
 * - Integration with Bucket4jRateLimiter
 * - Reactive flow handling
 */
class RateLimitingServiceTest {

    private lateinit var service: RateLimitingService
    private lateinit var rateLimiter: RateLimiter
    private lateinit var eventPublisher: ApplicationEventPublisher

    @BeforeEach
    fun setUp() {
        rateLimiter = mockk()
        eventPublisher = mockk()
        service = RateLimitingService(rateLimiter, eventPublisher)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should consume token with default BUSINESS strategy`() {
        // Given
        val identifier = "API:test-key"
        val endpoint = "/api/business/data"
        val expectedResult = RateLimitResult.Allowed(
            remainingTokens = 99,
            limitCapacity = 100,
            resetTime = Instant.now().plusSeconds(3600),
        )

        every {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS)
        } returns Mono.just(expectedResult)

        // When/Then
        StepVerifier.create(service.consumeToken(identifier, endpoint))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 99
            }
            .verifyComplete()

        verify(exactly = 1) {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS)
        }
        verify(exactly = 0) { eventPublisher.publishEvent(any<RateLimitExceededEvent>()) }
    }

    @Test
    fun `should consume token with specific AUTH strategy`() {
        // Given
        val identifier = "IP:192.168.1.1"
        val endpoint = "/api/auth/login"
        val expectedResult = RateLimitResult.Allowed(
            remainingTokens = 9,
            limitCapacity = 10,
            resetTime = Instant.now().plusSeconds(60),
        )

        every {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH)
        } returns Mono.just(expectedResult)

        // When/Then
        StepVerifier.create(service.consumeToken(identifier, endpoint, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 9
            }
            .verifyComplete()

        verify(exactly = 1) {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH)
        }
        verify(exactly = 0) { eventPublisher.publishEvent(any<RateLimitExceededEvent>()) }
    }

    @Test
    fun `should publish event when rate limit is exceeded`() {
        // Given
        val identifier = "IP:192.168.1.2"
        val endpoint = "/api/auth/login"
        val retryAfter = Duration.ofMinutes(5)
        val expectedResult = RateLimitResult.Denied(
            retryAfter = retryAfter,
            limitCapacity = 10,
        )

        every {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH)
        } returns Mono.just(expectedResult)

        every { eventPublisher.publishEvent(any<RateLimitExceededEvent>()) } just Runs

        // When/Then
        StepVerifier.create(service.consumeToken(identifier, endpoint, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Denied>()
                result.retryAfter shouldBe retryAfter
            }
            .verifyComplete()

        verify(exactly = 1) {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH)
        }

        verify(exactly = 1) {
            eventPublisher.publishEvent(
                withArg<RateLimitExceededEvent> { event ->
                    event.identifier shouldBe identifier
                    event.endpoint shouldBe endpoint
                    event.windowDuration shouldBe retryAfter
                },
            )
        }
    }

    @Test
    fun `should not publish event when rate limit is not exceeded`() {
        // Given
        val identifier = "IP:192.168.1.3"
        val endpoint = "/api/auth/login"
        val expectedResult = RateLimitResult.Allowed(
            remainingTokens = 5,
            limitCapacity = 10,
            resetTime = Instant.now().plusSeconds(60),
        )

        every {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH)
        } returns Mono.just(expectedResult)

        // When
        service.consumeToken(identifier, endpoint, RateLimitStrategy.AUTH).block()

        // Then
        verify(exactly = 1) {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH)
        }
        verify(exactly = 0) { eventPublisher.publishEvent(any<RateLimitExceededEvent>()) }
    }

    @Test
    fun `should publish event with BUSINESS strategy when limit exceeded`() {
        // Given
        val identifier = "API:test-key"
        val endpoint = "/api/business/data"
        val retryAfter = Duration.ofHours(1)
        val expectedResult = RateLimitResult.Denied(
            retryAfter = retryAfter,
            limitCapacity = 100,
        )

        every {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS)
        } returns Mono.just(expectedResult)

        every { eventPublisher.publishEvent(any<RateLimitExceededEvent>()) } just Runs

        // When/Then
        StepVerifier.create(service.consumeToken(identifier, endpoint, RateLimitStrategy.BUSINESS))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Denied>()
            }
            .verifyComplete()

        verify(exactly = 1) {
            eventPublisher.publishEvent(
                withArg<RateLimitExceededEvent> { event ->
                    event.identifier shouldBe identifier
                    event.endpoint shouldBe endpoint
                    event.windowDuration shouldBe retryAfter
                },
            )
        }
    }

    @Test
    fun `should handle multiple consecutive allowed requests`() {
        // Given
        val identifier = "API:test-key"
        val endpoint = "/api/business/data"
        val resetTime = Instant.now().plusSeconds(3600)

        every {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS)
        } returns Mono.just(
            RateLimitResult.Allowed(
                remainingTokens = 10,
                limitCapacity = 100,
                resetTime = resetTime,
            ),
        ) andThen
            Mono.just(
                RateLimitResult.Allowed(
                    remainingTokens = 9,
                    limitCapacity = 100,
                    resetTime = resetTime,
                ),
            ) andThen
            Mono.just(
                RateLimitResult.Allowed(
                    remainingTokens = 8,
                    limitCapacity = 100,
                    resetTime = resetTime,
                ),
            )

        // When/Then - First request
        StepVerifier.create(service.consumeToken(identifier, endpoint))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 10
            }
            .verifyComplete()

        // When/Then - Second request
        StepVerifier.create(service.consumeToken(identifier, endpoint))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 9
            }
            .verifyComplete()

        // When/Then - Third request
        StepVerifier.create(service.consumeToken(identifier, endpoint))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 8
            }
            .verifyComplete()

        verify(exactly = 3) {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS)
        }
        verify(exactly = 0) { eventPublisher.publishEvent(any<RateLimitExceededEvent>()) }
    }

    @Test
    fun `should handle transition from allowed to denied`() {
        // Given
        val identifier = "API:test-key"
        val endpoint = "/api/business/data"
        val retryAfter = Duration.ofHours(1)

        every {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS)
        } returns Mono.just(
            RateLimitResult.Allowed(
                1,
                100,
                Instant.now().plusSeconds(3600),
            ),
        ) andThen
            Mono.just(RateLimitResult.Denied(retryAfter, 100))

        every { eventPublisher.publishEvent(any<RateLimitExceededEvent>()) } just Runs

        // When/Then - First request (allowed)
        StepVerifier.create(service.consumeToken(identifier, endpoint))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
            }
            .verifyComplete()

        // When/Then - Second request (denied)
        StepVerifier.create(service.consumeToken(identifier, endpoint))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Denied>()
                result.retryAfter shouldBe retryAfter
            }
            .verifyComplete()

        verify(exactly = 2) {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS)
        }
        verify(exactly = 1) { eventPublisher.publishEvent(any<RateLimitExceededEvent>()) }
    }

    @Test
    fun `should propagate errors from rate limiter`() {
        // Given
        val identifier = "API:test-key"
        val endpoint = "/api/business/data"
        val error = RuntimeException("Rate limiter error")

        every {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS)
        } returns Mono.error(error)

        // When/Then
        StepVerifier.create(service.consumeToken(identifier, endpoint))
            .expectError(RuntimeException::class.java)
            .verify()

        verify(exactly = 0) { eventPublisher.publishEvent(any<RateLimitExceededEvent>()) }
    }

    @Test
    fun `should handle different identifiers independently`() {
        // Given
        val identifier1 = "API:key-1"
        val identifier2 = "API:key-2"
        val endpoint = "/api/business/data"

        every {
            rateLimiter.consumeToken(identifier1, RateLimitStrategy.BUSINESS)
        } returns Mono.just(RateLimitResult.Allowed(10, 100, Instant.now().plusSeconds(3600)))

        every {
            rateLimiter.consumeToken(identifier2, RateLimitStrategy.BUSINESS)
        } returns Mono.just(RateLimitResult.Denied(Duration.ofHours(1), 100))

        every { eventPublisher.publishEvent(any<RateLimitExceededEvent>()) } just Runs

        // When/Then - identifier1 (allowed)
        StepVerifier.create(service.consumeToken(identifier1, endpoint))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
            }
            .verifyComplete()

        // When/Then - identifier2 (denied)
        StepVerifier.create(service.consumeToken(identifier2, endpoint))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Denied>()
            }
            .verifyComplete()

        verify(exactly = 1) {
            rateLimiter.consumeToken(identifier1, RateLimitStrategy.BUSINESS)
        }
        verify(exactly = 1) {
            rateLimiter.consumeToken(identifier2, RateLimitStrategy.BUSINESS)
        }
        verify(exactly = 1) { eventPublisher.publishEvent(any<RateLimitExceededEvent>()) }
    }
}
