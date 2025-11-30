package com.cvix.ratelimit

import com.cvix.ratelimit.application.RateLimitingService
import com.cvix.ratelimit.domain.RateLimitResult
import com.cvix.ratelimit.domain.RateLimitStrategy
import com.cvix.ratelimit.infrastructure.RateLimitingFilter
import com.cvix.ratelimit.infrastructure.config.BucketConfigurationStrategy
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.InetSocketAddress
import java.time.Duration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Unit tests for RateLimitingFilter.
 *
 * Tests cover:
 * - Filter behavior for authentication endpoints
 * - Filter bypass for non-auth endpoints
 * - Rate limit headers addition
 * - Rate limit error response generation
 * - IP address extraction (including X-Forwarded-For)
 * - Enable/disable configuration
 * - Request already processed handling
 */
class RateLimitingFilterTest {

    private lateinit var filter: RateLimitingFilter
    private lateinit var rateLimitingService: RateLimitingService
    private lateinit var configurationStrategy: BucketConfigurationStrategy
    private lateinit var objectMapper: ObjectMapper
    private lateinit var chain: WebFilterChain

    @BeforeEach
    fun setUp() {
        rateLimitingService = mockk()
        configurationStrategy = mockk()
        objectMapper = ObjectMapper()
        chain = mockk()

        filter = RateLimitingFilter(rateLimitingService, objectMapper, configurationStrategy)

        // Default mocks
        every { configurationStrategy.isAuthRateLimitEnabled() } returns true
        every { configurationStrategy.isResumeRateLimitEnabled() } returns true
        every {
            configurationStrategy.getAuthEndpoints()
        } returns listOf("/api/auth/login", "/api/auth/register")
        every {
            configurationStrategy.getResumeEndpoints()
        } returns listOf("/api/resume/generate")
        every { chain.filter(any()) } returns Mono.empty()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should allow request when rate limit not exceeded`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login")
            .remoteAddress(InetSocketAddress("127.0.0.1", 8080))
            .build()
        val exchange = MockServerWebExchange.from(request)
        val identifier = "IP:127.0.0.1"

        every {
            rateLimitingService.consumeToken(identifier, "/api/auth/login", RateLimitStrategy.AUTH)
        } returns Mono.just(RateLimitResult.Allowed(remainingTokens = 9))

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) { chain.filter(exchange) }
        verify(exactly = 1) {
            rateLimitingService.consumeToken(identifier, "/api/auth/login", RateLimitStrategy.AUTH)
        }

        exchange.response.headers["X-Rate-Limit-Remaining"]?.get(0) shouldBe "9"
    }

    @Test
    fun `should deny request when rate limit exceeded`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login")
            .remoteAddress(InetSocketAddress("127.0.0.1", 8080))
            .build()
        val exchange = MockServerWebExchange.from(request)
        val identifier = "IP:127.0.0.1"
        val retryAfter = Duration.ofMinutes(5)

        every {
            rateLimitingService.consumeToken(identifier, "/api/auth/login", RateLimitStrategy.AUTH)
        } returns Mono.just(RateLimitResult.Denied(retryAfter = retryAfter))

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 0) { chain.filter(exchange) }
        verify(exactly = 1) {
            rateLimitingService.consumeToken(identifier, "/api/auth/login", RateLimitStrategy.AUTH)
        }

        exchange.response.statusCode shouldBe HttpStatus.TOO_MANY_REQUESTS
        exchange.response.headers["X-Rate-Limit-Retry-After-Seconds"]?.get(0) shouldBe "300"
    }

    @Test
    fun `should skip rate limiting for non-authentication endpoints`() {
        // Given
        val request = MockServerHttpRequest.get("/api/users/profile").build()
        val exchange = MockServerWebExchange.from(request)

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) { chain.filter(exchange) }
        verify(exactly = 0) { rateLimitingService.consumeToken(any(), any(), any()) }
    }

    @Test
    fun `should skip rate limiting when auth rate limiting is disabled`() {
        // Given
        every { configurationStrategy.isAuthRateLimitEnabled() } returns false
        val request = MockServerHttpRequest.post("/api/auth/login").build()
        val exchange = MockServerWebExchange.from(request)

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) { chain.filter(exchange) }
        verify(exactly = 0) { rateLimitingService.consumeToken(any(), any(), any()) }
    }

    @Test
    fun `should extract IP from X-Forwarded-For header`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login")
            .header("X-Forwarded-For", "203.0.113.1, 198.51.100.1")
            .build()
        val exchange = MockServerWebExchange.from(request)
        val expectedIdentifier = "IP:203.0.113.1" // Should use first IP

        every {
            rateLimitingService.consumeToken(expectedIdentifier, "/api/auth/login", RateLimitStrategy.AUTH)
        } returns Mono.just(RateLimitResult.Allowed(remainingTokens = 9))

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) {
            rateLimitingService.consumeToken(expectedIdentifier, "/api/auth/login", RateLimitStrategy.AUTH)
        }
    }

    @Test
    fun `should use remote address when X-Forwarded-For is not present`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login")
            .remoteAddress(InetSocketAddress("192.168.1.100", 8080))
            .build()
        val exchange = MockServerWebExchange.from(request)
        val expectedIdentifier = "IP:192.168.1.100"

        every {
            rateLimitingService.consumeToken(expectedIdentifier, "/api/auth/login", RateLimitStrategy.AUTH)
        } returns Mono.just(RateLimitResult.Allowed(remainingTokens = 9))

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) {
            rateLimitingService.consumeToken(expectedIdentifier, "/api/auth/login", RateLimitStrategy.AUTH)
        }
    }

    @Test
    fun `should handle register endpoint correctly`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/register")
            .remoteAddress(InetSocketAddress("127.0.0.1", 8080))
            .build()
        val exchange = MockServerWebExchange.from(request)
        val identifier = "IP:127.0.0.1"

        every {
            rateLimitingService.consumeToken(identifier, "/api/auth/register", RateLimitStrategy.AUTH)
        } returns Mono.just(RateLimitResult.Allowed(remainingTokens = 8))

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) {
            rateLimitingService.consumeToken(identifier, "/api/auth/register", RateLimitStrategy.AUTH)
        }
    }

    @Test
    fun `should add rate limit headers with remaining tokens`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login").build()
        val exchange = MockServerWebExchange.from(request)
        val remainingTokens = 42L

        every {
            rateLimitingService.consumeToken(any(), any(), RateLimitStrategy.AUTH)
        } returns Mono.just(RateLimitResult.Allowed(remainingTokens = remainingTokens))

        // When
        filter.filter(exchange, chain).block()

        // Then
        exchange.response.headers["X-Rate-Limit-Remaining"]?.get(0) shouldBe remainingTokens.toString()
    }

    @Test
    fun `should add retry-after header when rate limit exceeded`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login").build()
        val exchange = MockServerWebExchange.from(request)
        val retryAfterSeconds = 600L

        every {
            rateLimitingService.consumeToken(any(), any(), RateLimitStrategy.AUTH)
        } returns Mono.just(RateLimitResult.Denied(retryAfter = Duration.ofSeconds(retryAfterSeconds)))

        // When
        filter.filter(exchange, chain).block()

        // Then
        exchange.response.headers["X-Rate-Limit-Retry-After-Seconds"]?.get(0) shouldBe retryAfterSeconds.toString()
    }

    @Test
    fun `should return JSON error response when rate limit exceeded`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login").build()
        val exchange = MockServerWebExchange.from(request)

        every {
            rateLimitingService.consumeToken(any(), any(), RateLimitStrategy.AUTH)
        } returns Mono.just(RateLimitResult.Denied(retryAfter = Duration.ofMinutes(5)))

        // When
        filter.filter(exchange, chain).block()

        // Then
        exchange.response.statusCode shouldBe HttpStatus.TOO_MANY_REQUESTS
        val contentType = exchange.response.headers.contentType
        contentType?.toString() shouldBe "application/json"
    }

    @Test
    fun `should skip processing if request was already processed`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login").build()
        val exchange = MockServerWebExchange.from(request)

        // Mark as already processed
        exchange.attributes["rateLimitProcessed"] = true

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) { chain.filter(exchange) }
        verify(exactly = 0) { rateLimitingService.consumeToken(any(), any(), any()) }
    }

    @Test
    fun `should handle endpoints with query parameters`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login?redirect=/dashboard").build()
        val exchange = MockServerWebExchange.from(request)

        every {
            rateLimitingService.consumeToken(any(), any(), RateLimitStrategy.AUTH)
        } returns Mono.just(RateLimitResult.Allowed(remainingTokens = 9))

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) { chain.filter(exchange) }
    }

    @Test
    fun `should handle different HTTP methods correctly`() {
        // Given - POST request
        val postRequest = MockServerHttpRequest.post("/api/auth/login").build()
        val postExchange = MockServerWebExchange.from(postRequest)

        every {
            rateLimitingService.consumeToken(any(), any(), RateLimitStrategy.AUTH)
        } returns Mono.just(RateLimitResult.Allowed(remainingTokens = 9))

        // When - POST
        filter.filter(postExchange, chain).block()

        // Then
        verify(exactly = 1) {
            rateLimitingService.consumeToken(any(), "/api/auth/login", RateLimitStrategy.AUTH)
        }

        clearMocks(rateLimitingService, answers = false)

        // Given - GET request (less common for auth, but should still work)
        val getRequest = MockServerHttpRequest.get("/api/auth/login").build()
        val getExchange = MockServerWebExchange.from(getRequest)

        every {
            rateLimitingService.consumeToken(any(), any(), RateLimitStrategy.AUTH)
        } returns Mono.just(RateLimitResult.Allowed(remainingTokens = 8))

        // When - GET
        filter.filter(getExchange, chain).block()

        // Then
        verify(exactly = 1) {
            rateLimitingService.consumeToken(any(), "/api/auth/login", RateLimitStrategy.AUTH)
        }
    }

    @Test
    fun `should handle path with trailing slash`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login/").build()
        val exchange = MockServerWebExchange.from(request)

        every {
            rateLimitingService.consumeToken(any(), any(), RateLimitStrategy.AUTH)
        } returns Mono.just(RateLimitResult.Allowed(remainingTokens = 9))

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) { chain.filter(exchange) }
    }

    @Test
    fun `should use unknown IP when remote address is not available`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login").build()
        val exchange = MockServerWebExchange.from(request)
        val expectedIdentifier = "IP:unknown"

        every {
            rateLimitingService.consumeToken(expectedIdentifier, "/api/auth/login", RateLimitStrategy.AUTH)
        } returns Mono.just(RateLimitResult.Allowed(remainingTokens = 9))

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()
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
            rateLimitingService.consumeToken(identifier, "/api/resume/generate", RateLimitStrategy.RESUME)
        } returns Mono.just(RateLimitResult.Allowed(remainingTokens = 9))

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) {
            rateLimitingService.consumeToken(identifier, "/api/resume/generate", RateLimitStrategy.RESUME)
        }
    }

    @Test
    fun `should skip rate limiting for resume endpoints when disabled`() {
        // Given
        every { configurationStrategy.isResumeRateLimitEnabled() } returns false
        val request = MockServerHttpRequest.post("/api/resume/generate").build()
        val exchange = MockServerWebExchange.from(request)

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 1) { chain.filter(exchange) }
        verify(exactly = 0) { rateLimitingService.consumeToken(any(), any(), any()) }
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
            rateLimitingService.consumeToken(identifier, "/api/resume/generate", RateLimitStrategy.RESUME)
        } returns Mono.just(RateLimitResult.Denied(retryAfter = retryAfter))

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 0) { chain.filter(exchange) }
        verify(exactly = 1) {
            rateLimitingService.consumeToken(identifier, "/api/resume/generate", RateLimitStrategy.RESUME)
        }

        exchange.response.statusCode shouldBe HttpStatus.TOO_MANY_REQUESTS
        exchange.response.headers["X-Rate-Limit-Retry-After-Seconds"]?.get(0) shouldBe "300"

        // Verify response body contains resume-specific error message
        val responseBody = exchange.response.bodyAsString.block()
        responseBody shouldContain "Rate limit exceeded for resume generation. Please try again later."
    }
}
