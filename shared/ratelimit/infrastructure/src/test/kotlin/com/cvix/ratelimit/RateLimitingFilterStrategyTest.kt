package com.cvix.ratelimit

import com.cvix.UnitTest
import com.cvix.ratelimit.domain.RateLimitResult
import com.cvix.ratelimit.domain.RateLimitStrategy
import com.cvix.ratelimit.infrastructure.adapter.ReactiveRateLimitingAdapter
import com.cvix.ratelimit.infrastructure.config.BucketConfigurationFactory
import com.cvix.ratelimit.infrastructure.filter.RateLimitingFilter
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule

/**
 * Unit tests for rate limiting strategy handling in [RateLimitingFilter].
 *
 * Tests cover:
 * - RESUME strategy endpoints
 * - WAITLIST strategy endpoints
 * - Strategy-specific error messages
 * - Enable/disable per strategy
 */
@UnitTest
internal class RateLimitingFilterStrategyTest {

    private lateinit var filter: RateLimitingFilter
    private lateinit var reactiveRateLimitingAdapter: ReactiveRateLimitingAdapter
    private lateinit var configurationFactory: BucketConfigurationFactory
    private lateinit var jsonMapper: JsonMapper
    private lateinit var chain: WebFilterChain

    @BeforeEach
    fun setUp() {
        reactiveRateLimitingAdapter = mockk()
        configurationFactory = mockk()
        jsonMapper = jsonMapper { addModule(kotlinModule()) }
        chain = mockk()

        filter = RateLimitingFilter(reactiveRateLimitingAdapter, jsonMapper, configurationFactory)

        // Default mocks
        every { configurationFactory.isRateLimitEnabled(RateLimitStrategy.AUTH) } returns false
        every { configurationFactory.isRateLimitEnabled(RateLimitStrategy.RESUME) } returns true
        every { configurationFactory.isRateLimitEnabled(RateLimitStrategy.WAITLIST) } returns true
        every {
            configurationFactory.getEndpoints(RateLimitStrategy.AUTH)
        } returns listOf("/api/auth/login")
        every {
            configurationFactory.getEndpoints(RateLimitStrategy.RESUME)
        } returns listOf("/api/resume/generate")
        every {
            configurationFactory.getEndpoints(RateLimitStrategy.WAITLIST)
        } returns listOf("/api/waitlist")
        every { chain.filter(any()) } returns Mono.empty()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should apply rate limit to resume endpoints`() {
        // Given
        val request = MockServerHttpRequest.post("/api/resume/generate")
            .remoteAddress(InetSocketAddress("127.0.0.1", 8080))
            .build()
        val exchange = MockServerWebExchange.from(request)
        val identifier = "IP:127.0.0.1"

        every {
            reactiveRateLimitingAdapter.consumeToken(
                identifier,
                "/api/resume/generate",
                RateLimitStrategy.RESUME,
            )
        } returns Mono.just(
            RateLimitResult.Allowed(
                remainingTokens = 9,
                limitCapacity = 10,
                resetTime = Instant.now().plusSeconds(60),
            ),
        )

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) {
            reactiveRateLimitingAdapter.consumeToken(
                identifier,
                "/api/resume/generate",
                RateLimitStrategy.RESUME,
            )
        }
    }

    @Test
    fun `should skip rate limiting for resume endpoints when disabled`() {
        // Given
        every { configurationFactory.isRateLimitEnabled(RateLimitStrategy.RESUME) } returns false
        val request = MockServerHttpRequest.post("/api/resume/generate").build()
        val exchange = MockServerWebExchange.from(request)

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) { chain.filter(exchange) }
        verify(exactly = 0) { reactiveRateLimitingAdapter.consumeToken(any(), any(), any()) }
    }

    @Test
    fun `should return error when rate limit exceeded for resume endpoints`() {
        // Given
        val request = MockServerHttpRequest.post("/api/resume/generate")
            .remoteAddress(InetSocketAddress("127.0.0.1", 8080))
            .build()
        val exchange = MockServerWebExchange.from(request)
        val identifier = "IP:127.0.0.1"
        val retryAfter = Duration.ofMinutes(5)

        every {
            reactiveRateLimitingAdapter.consumeToken(
                identifier,
                "/api/resume/generate",
                RateLimitStrategy.RESUME,
            )
        } returns Mono.just(RateLimitResult.Denied(retryAfter = retryAfter, limitCapacity = 10))

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 0) { chain.filter(exchange) }
        verify(exactly = 1) {
            reactiveRateLimitingAdapter.consumeToken(
                identifier,
                "/api/resume/generate",
                RateLimitStrategy.RESUME,
            )
        }

        exchange.response.statusCode shouldBe HttpStatus.TOO_MANY_REQUESTS
        exchange.response.headers["X-Rate-Limit-Retry-After-Seconds"]?.get(0) shouldBe "300"

        // Verify response body contains resume-specific error message
        val responseBody = exchange.response.bodyAsString.block()
        responseBody shouldContain "Rate limit exceeded for resume generation. Please try again later."
    }

    @Test
    fun `should apply rate limit to waitlist endpoint`() {
        // Given
        val request = MockServerHttpRequest.post("/api/waitlist")
            .remoteAddress(InetSocketAddress("127.0.0.1", 8080))
            .build()
        val exchange = MockServerWebExchange.from(request)
        val identifier = "IP:127.0.0.1"

        every {
            reactiveRateLimitingAdapter.consumeToken(
                identifier,
                "/api/waitlist",
                RateLimitStrategy.WAITLIST,
            )
        } returns Mono.just(
            RateLimitResult.Allowed(
                remainingTokens = 4,
                limitCapacity = 5,
                resetTime = Instant.now().plusSeconds(60),
            ),
        )

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) {
            reactiveRateLimitingAdapter.consumeToken(
                identifier,
                "/api/waitlist",
                RateLimitStrategy.WAITLIST,
            )
        }
        verify(exactly = 1) { chain.filter(exchange) }
        exchange.response.headers["X-RateLimit-Limit"]?.get(0) shouldBe "5"
        exchange.response.headers["X-RateLimit-Remaining"]?.get(0) shouldBe "4"
        exchange.response.headers["X-RateLimit-Reset"]?.get(0) shouldNotBe null
    }

    @Test
    fun `should return error when rate limit exceeded for waitlist endpoint`() {
        // Given
        val request = MockServerHttpRequest.post("/api/waitlist")
            .remoteAddress(InetSocketAddress("127.0.0.1", 8080))
            .build()
        val exchange = MockServerWebExchange.from(request)
        val identifier = "IP:127.0.0.1"
        val retryAfter = Duration.ofMinutes(10)

        every {
            reactiveRateLimitingAdapter.consumeToken(
                identifier,
                "/api/waitlist",
                RateLimitStrategy.WAITLIST,
            )
        } returns Mono.just(RateLimitResult.Denied(retryAfter = retryAfter, limitCapacity = 5))

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) {
            reactiveRateLimitingAdapter.consumeToken(
                identifier,
                "/api/waitlist",
                RateLimitStrategy.WAITLIST,
            )
        }
        verify(exactly = 0) { chain.filter(exchange) }

        exchange.response.statusCode shouldBe HttpStatus.TOO_MANY_REQUESTS
        exchange.response.headers["Retry-After"]?.get(0) shouldBe "600"
        exchange.response.headers["X-RateLimit-Limit"]?.get(0) shouldBe "5"
        exchange.response.headers["X-Rate-Limit-Retry-After-Seconds"]?.get(0) shouldBe "600"
    }

    @Test
    fun `should match endpoints with trailing slashes in configuration`() {
        // Given - endpoint configured with trailing slash /api/resume/generate/
        every {
            configurationFactory.getEndpoints(RateLimitStrategy.RESUME)
        } returns listOf("/api/resume/generate/")

        val request = MockServerHttpRequest.post("/api/resume/generate")
            .remoteAddress(InetSocketAddress("127.0.0.1", 8080))
            .build()
        val exchange = MockServerWebExchange.from(request)
        val identifier = "IP:127.0.0.1"

        every {
            reactiveRateLimitingAdapter.consumeToken(
                identifier,
                "/api/resume/generate",
                RateLimitStrategy.RESUME,
            )
        } returns Mono.just(
            RateLimitResult.Allowed(
                remainingTokens = 9,
                limitCapacity = 10,
                resetTime = Instant.now().plusSeconds(60),
            ),
        )

        // When
        val result = filter.filter(exchange, chain)

        // Then - should match despite trailing slash difference
        StepVerifier.create(result).verifyComplete()

        verify(exactly = 1) {
            reactiveRateLimitingAdapter.consumeToken(
                identifier,
                "/api/resume/generate",
                RateLimitStrategy.RESUME,
            )
        }
    }
}
