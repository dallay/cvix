package com.cvix.ratelimit

import com.cvix.UnitTest
import com.cvix.ratelimit.domain.RateLimitResult
import com.cvix.ratelimit.domain.RateLimitStrategy
import com.cvix.ratelimit.infrastructure.adapter.ReactiveRateLimitingAdapter
import com.cvix.ratelimit.infrastructure.config.BucketConfigurationFactory
import com.cvix.ratelimit.infrastructure.filter.RateLimitingFilter
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.InetSocketAddress
import java.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule

/**
 * Unit tests for IP address extraction and sanitization in [RateLimitingFilter].
 *
 * Tests cover:
 * - X-Forwarded-For header extraction
 * - Remote address fallback
 * - Log injection prevention (sanitization)
 * - IPv6 support
 * - Edge cases (long values, special characters)
 */
@UnitTest
internal class RateLimitingFilterIpExtractionTest {

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
        every { configurationFactory.isRateLimitEnabled(RateLimitStrategy.RESUME) } returns false
        every { configurationFactory.isRateLimitEnabled(RateLimitStrategy.WAITLIST) } returns false
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

        verify(exactly = 1) {
            reactiveRateLimitingAdapter.consumeToken(
                expectedIdentifier,
                "/api/auth/login",
                RateLimitStrategy.AUTH,
            )
        }
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
