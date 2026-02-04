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
import io.mockk.clearMocks
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
@UnitTest
internal class RateLimitingFilterTest {

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
        every { configurationFactory.isRateLimitEnabled(RateLimitStrategy.AUTH) } returns true
        every { configurationFactory.isRateLimitEnabled(RateLimitStrategy.RESUME) } returns true
        every { configurationFactory.isRateLimitEnabled(RateLimitStrategy.WAITLIST) } returns true
        every {
            configurationFactory.getEndpoints(RateLimitStrategy.AUTH)
        } returns listOf("/api/auth/login", "/api/auth/register")
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
    fun `should allow request when rate limit not exceeded`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login")
            .remoteAddress(InetSocketAddress("127.0.0.1", 8080))
            .build()
        val exchange = MockServerWebExchange.from(request)
        val identifier = "IP:127.0.0.1"

        every {
            reactiveRateLimitingAdapter.consumeToken(identifier, "/api/auth/login", RateLimitStrategy.AUTH)
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

        verify(exactly = 1) { chain.filter(exchange) }
        verify(exactly = 1) {
            reactiveRateLimitingAdapter.consumeToken(identifier, "/api/auth/login", RateLimitStrategy.AUTH)
        }

        // Verify standard rate limit headers
        exchange.response.headers["X-RateLimit-Limit"]?.get(0) shouldBe "10"
        exchange.response.headers["X-RateLimit-Remaining"]?.get(0) shouldBe "9"
        // X-RateLimit-Reset should be present (Unix timestamp)
        exchange.response.headers["X-RateLimit-Reset"]?.get(0) shouldNotBe null
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
            reactiveRateLimitingAdapter.consumeToken(identifier, "/api/auth/login", RateLimitStrategy.AUTH)
        } returns Mono.just(RateLimitResult.Denied(retryAfter = retryAfter, limitCapacity = 10))

        // When
        val result = filter.filter(exchange, chain)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(exactly = 0) { chain.filter(exchange) }
        verify(exactly = 1) {
            reactiveRateLimitingAdapter.consumeToken(identifier, "/api/auth/login", RateLimitStrategy.AUTH)
        }

        exchange.response.statusCode shouldBe HttpStatus.TOO_MANY_REQUESTS
        // Verify standard HTTP Retry-After header
        exchange.response.headers["Retry-After"]?.get(0) shouldBe "300"
        // Verify rate limit headers
        exchange.response.headers["X-RateLimit-Limit"]?.get(0) shouldBe "10"
        // Backward compatibility header (deprecated)
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
        verify(exactly = 0) { reactiveRateLimitingAdapter.consumeToken(any(), any(), any()) }
    }

    @Test
    fun `should skip rate limiting when auth rate limiting is disabled`() {
        // Given
        every { configurationFactory.isRateLimitEnabled(RateLimitStrategy.AUTH) } returns false
        val request = MockServerHttpRequest.post("/api/auth/login").build()
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
    fun `should extract IP from X-Forwarded-For header`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login")
            .header("X-Forwarded-For", "203.0.113.1, 198.51.100.1")
            .build()
        val exchange = MockServerWebExchange.from(request)
        val expectedIdentifier = "IP:203.0.113.1" // Should use first IP

        every {
            reactiveRateLimitingAdapter.consumeToken(
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
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
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
            )
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
            reactiveRateLimitingAdapter.consumeToken(
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
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
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
            )
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
            reactiveRateLimitingAdapter.consumeToken(
                identifier,
                "/api/auth/register",
                RateLimitStrategy.AUTH,
            )
        } returns Mono.just(
            RateLimitResult.Allowed(
                remainingTokens = 8,
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
                "/api/auth/register",
                RateLimitStrategy.AUTH,
            )
        }
    }

    @Test
    fun `should add rate limit headers with remaining tokens`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login").build()
        val exchange = MockServerWebExchange.from(request)
        val remainingTokens = 42L

        every {
            reactiveRateLimitingAdapter.consumeToken(any(), any(), RateLimitStrategy.AUTH)
        } returns Mono.just(
            RateLimitResult.Allowed(
                remainingTokens = remainingTokens,
                limitCapacity = 100,
                resetTime = Instant.now().plusSeconds(3600),
            ),
        )

        // When
        filter.filter(exchange, chain).block()

        // Then
        exchange.response.headers["X-RateLimit-Remaining"]?.get(0) shouldBe remainingTokens.toString()
    }

    @Test
    fun `should add retry-after header when rate limit exceeded`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login").build()
        val exchange = MockServerWebExchange.from(request)
        val retryAfterSeconds = 600L

        every {
            reactiveRateLimitingAdapter.consumeToken(any(), any(), RateLimitStrategy.AUTH)
        } returns Mono.just(
            RateLimitResult.Denied(
                retryAfter = Duration.ofSeconds(retryAfterSeconds),
                limitCapacity = 10,
            ),
        )

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
            reactiveRateLimitingAdapter.consumeToken(any(), any(), RateLimitStrategy.AUTH)
        } returns Mono.just(
            RateLimitResult.Denied(
                retryAfter = Duration.ofMinutes(5),
                limitCapacity = 10,
            ),
        )

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
        verify(exactly = 0) { reactiveRateLimitingAdapter.consumeToken(any(), any(), any()) }
    }

    @Test
    fun `should handle endpoints with query parameters`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login?redirect=/dashboard").build()
        val exchange = MockServerWebExchange.from(request)

        every {
            reactiveRateLimitingAdapter.consumeToken(any(), any(), RateLimitStrategy.AUTH)
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

        verify(exactly = 1) { chain.filter(exchange) }
    }

    @Test
    fun `should handle different HTTP methods correctly`() {
        // Given - POST request
        val postRequest = MockServerHttpRequest.post("/api/auth/login").build()
        val postExchange = MockServerWebExchange.from(postRequest)

        every {
            reactiveRateLimitingAdapter.consumeToken(any(), any(), RateLimitStrategy.AUTH)
        } returns Mono.just(
            RateLimitResult.Allowed(
                remainingTokens = 9,
                limitCapacity = 10,
                resetTime = Instant.now().plusSeconds(60),
            ),
        )

        // When - POST
        filter.filter(postExchange, chain).block()

        // Then
        verify(exactly = 1) {
            reactiveRateLimitingAdapter.consumeToken(any(), "/api/auth/login", RateLimitStrategy.AUTH)
        }

        clearMocks(reactiveRateLimitingAdapter, answers = false)

        // Given - GET request (less common for auth, but should still work)
        val getRequest = MockServerHttpRequest.get("/api/auth/login").build()
        val getExchange = MockServerWebExchange.from(getRequest)

        every {
            reactiveRateLimitingAdapter.consumeToken(any(), any(), RateLimitStrategy.AUTH)
        } returns Mono.just(
            RateLimitResult.Allowed(
                remainingTokens = 8,
                limitCapacity = 10,
                resetTime = Instant.now().plusSeconds(60),
            ),
        )

        // When - GET
        filter.filter(getExchange, chain).block()

        // Then
        verify(exactly = 1) {
            reactiveRateLimitingAdapter.consumeToken(any(), "/api/auth/login", RateLimitStrategy.AUTH)
        }
    }

    @Test
    fun `should handle path with trailing slash`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login/").build()
        val exchange = MockServerWebExchange.from(request)

        every {
            reactiveRateLimitingAdapter.consumeToken(any(), any(), RateLimitStrategy.AUTH)
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

        verify(exactly = 1) { chain.filter(exchange) }
    }

    @Test
    fun `should use unknown IP when remote address is not available`() {
        // Given
        val request = MockServerHttpRequest.post("/api/auth/login").build()
        val exchange = MockServerWebExchange.from(request)
        val expectedIdentifier = "IP:unknown"

        every {
            reactiveRateLimitingAdapter.consumeToken(
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
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
    fun `should not match false positive paths with contains logic`() {
        // Given - endpoint is /api/auth but path should NOT match /api/auth-extended or /api/v2/auth/settings
        val request1 = MockServerHttpRequest.get("/api/auth-extended").build()
        val exchange1 = MockServerWebExchange.from(request1)

        val request2 = MockServerHttpRequest.get("/api/v2/auth/settings").build()
        val exchange2 = MockServerWebExchange.from(request2)

        // When
        val result1 = filter.filter(exchange1, chain)
        val result2 = filter.filter(exchange2, chain)

        // Then - should skip rate limiting for both (not recognized as auth endpoints)
        StepVerifier.create(result1).verifyComplete()
        StepVerifier.create(result2).verifyComplete()

        verify(exactly = 2) { chain.filter(any()) }
        verify(exactly = 0) { reactiveRateLimitingAdapter.consumeToken(any(), any(), any()) }
    }

    @Test
    fun `should match exact endpoint paths correctly`() {
        // Given - exact match for /api/auth/login
        val request = MockServerHttpRequest.post("/api/auth/login")
            .remoteAddress(InetSocketAddress("127.0.0.1", 8080))
            .build()
        val exchange = MockServerWebExchange.from(request)
        val identifier = "IP:127.0.0.1"

        every {
            reactiveRateLimitingAdapter.consumeToken(identifier, "/api/auth/login", RateLimitStrategy.AUTH)
        } returns Mono.just(
            RateLimitResult.Allowed(
                remainingTokens = 9,
                limitCapacity = 10,
                resetTime = Instant.now().plusSeconds(60),
            ),
        )

        // When
        val result = filter.filter(exchange, chain)

        // Then - should match and apply rate limiting
        StepVerifier.create(result).verifyComplete()

        verify(exactly = 1) {
            reactiveRateLimitingAdapter.consumeToken(identifier, "/api/auth/login", RateLimitStrategy.AUTH)
        }
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
        // Optionally assert response body for error message, if needed
    }

    // ==================== Log Injection Prevention Tests ====================
    // These tests verify that malicious X-Forwarded-For headers are sanitized
    // to prevent log injection attacks (CVE mitigation)

    @Test
    fun `should sanitize X-Forwarded-For header with newline characters to prevent log injection`() {
        // Given - Attacker attempts log injection via newline in X-Forwarded-For
        val maliciousIp = "192.168.1.1\n[INFO] Fake log entry - user authenticated successfully"
        val request = MockServerHttpRequest.post("/api/auth/login")
            .header("X-Forwarded-For", maliciousIp)
            .build()
        val exchange = MockServerWebExchange.from(request)

        // After sanitization: "192.168.1.1INFOFakelogentry-userauthenticatedsuccessfully"
        // newline, brackets, and spaces removed. Then truncated to MAX_IP_LENGTH (50 chars)
        val expectedIdentifier = "IP:192.168.1.1INFOFakelogentry-userauthenticatedsucce"

        every {
            reactiveRateLimitingAdapter.consumeToken(
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
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
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
            )
        }
    }

    @Test
    fun `should sanitize X-Forwarded-For header with carriage return characters`() {
        // Given - Attacker attempts log injection via carriage return
        val maliciousIp = "10.0.0.1\r[WARN] Security alert bypassed"
        val request = MockServerHttpRequest.post("/api/auth/login")
            .header("X-Forwarded-For", maliciousIp)
            .build()
        val exchange = MockServerWebExchange.from(request)

        // Expected: carriage return and brackets removed, truncated to MAX_IP_LENGTH
        val expectedIdentifier = "IP:10.0.0.1WARNSecurityalertbypassed"

        every {
            reactiveRateLimitingAdapter.consumeToken(
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
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
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
            )
        }
    }

    @Test
    fun `should allow valid IPv6 addresses in X-Forwarded-For header`() {
        // Given - Valid IPv6 address
        val ipv6Address = "2001:0db8:85a3:0000:0000:8a2e:0370:7334"
        val request = MockServerHttpRequest.post("/api/auth/login")
            .header("X-Forwarded-For", ipv6Address)
            .build()
        val exchange = MockServerWebExchange.from(request)

        val expectedIdentifier = "IP:$ipv6Address"

        every {
            reactiveRateLimitingAdapter.consumeToken(
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
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
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
            )
        }
    }

    @Test
    fun `should truncate excessively long X-Forwarded-For values`() {
        // Given - Excessively long IP value (potential DoS or buffer overflow attempt)
        val longIp = "192.168.1.1" + "a".repeat(100)
        val request = MockServerHttpRequest.post("/api/auth/login")
            .header("X-Forwarded-For", longIp)
            .build()
        val exchange = MockServerWebExchange.from(request)

        // Expected: truncated to MAX_IP_LENGTH (50 characters)
        val expectedIdentifier = "IP:" + "192.168.1.1" + "a".repeat(39) // 11 + 39 = 50 chars

        every {
            reactiveRateLimitingAdapter.consumeToken(
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
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
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
            )
        }
    }

    @Test
    fun `should sanitize X-Forwarded-For header with tab characters`() {
        // Given - Attacker attempts log injection via tab character
        val maliciousIp = "172.16.0.1\tinjected-data"
        val request = MockServerHttpRequest.post("/api/auth/login")
            .header("X-Forwarded-For", maliciousIp)
            .build()
        val exchange = MockServerWebExchange.from(request)

        // Expected: tab removed
        val expectedIdentifier = "IP:172.16.0.1injected-data"

        every {
            reactiveRateLimitingAdapter.consumeToken(
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
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
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
            )
        }
    }

    @Test
    fun `should sanitize X-Forwarded-For header with special characters`() {
        // Given - Attacker attempts injection with various special chars
        val maliciousIp = "10.0.0.1; DROP TABLE users; --"
        val request = MockServerHttpRequest.post("/api/auth/login")
            .header("X-Forwarded-For", maliciousIp)
            .build()
        val exchange = MockServerWebExchange.from(request)

        // Expected: semicolons, spaces, and special chars removed
        val expectedIdentifier = "IP:10.0.0.1DROPTABLEusers--"

        every {
            reactiveRateLimitingAdapter.consumeToken(
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
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
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
            )
        }
    }
}
