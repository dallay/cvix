package com.cvix.spring.boot.infrastructure.http

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.net.InetSocketAddress
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest

internal class ClientIpExtractorTest {

    @Nested
    inner class `Extract IP from Request` {

        @Test
        fun `should extract IP from X-Forwarded-For header when present`() {
            // Arrange
            val expectedIp = "203.0.113.45"
            val request = createMockRequest(
                xForwardedFor = expectedIp,
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }

        @Test
        fun `should extract first IP from X-Forwarded-For when multiple IPs present`() {
            // Arrange
            val expectedIp = "203.0.113.45"
            val request = createMockRequest(
                xForwardedFor = "$expectedIp, 10.0.0.1, 172.16.0.1",
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }

        @Test
        fun `should trim whitespace from X-Forwarded-For IP`() {
            // Arrange
            val expectedIp = "203.0.113.45"
            val request = createMockRequest(
                xForwardedFor = "  $expectedIp  ",
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }

        @Test
        fun `should extract IP from X-Real-IP header when X-Forwarded-For is absent`() {
            // Arrange
            val expectedIp = "203.0.113.45"
            val request = createMockRequest(
                xForwardedFor = null,
                xRealIp = expectedIp,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }

        @Test
        fun `should trim whitespace from X-Real-IP`() {
            // Arrange
            val expectedIp = "203.0.113.45"
            val request = createMockRequest(
                xForwardedFor = null,
                xRealIp = "  $expectedIp  ",
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }

        @Test
        fun `should fallback to remote address when headers are absent`() {
            // Arrange
            val expectedIp = "192.168.1.1"
            val request = createMockRequest(
                xForwardedFor = null,
                xRealIp = null,
                remoteAddress = expectedIp,
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }

        @Test
        fun `should fallback to remote address when X-Forwarded-For is blank`() {
            // Arrange
            val expectedIp = "192.168.1.1"
            val request = createMockRequest(
                xForwardedFor = "   ",
                xRealIp = null,
                remoteAddress = expectedIp,
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }

        @Test
        fun `should fallback to remote address when X-Real-IP is blank`() {
            // Arrange
            val expectedIp = "192.168.1.1"
            val request = createMockRequest(
                xForwardedFor = null,
                xRealIp = "   ",
                remoteAddress = expectedIp,
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }

        @Test
        fun `should return unknown when all sources are unavailable`() {
            // Arrange
            val request = createMockRequest(
                xForwardedFor = null,
                xRealIp = null,
                remoteAddress = null,
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe "unknown"
        }

        @Test
        fun `should extract IPv6 address from X-Forwarded-For`() {
            // Arrange
            val expectedIp = "2001:0db8:85a3:0000:0000:8a2e:0370:7334"
            val request = createMockRequest(
                xForwardedFor = expectedIp,
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }

        @Test
        fun `should extract compressed IPv6 address`() {
            // Arrange
            val expectedIp = "2001:db8::1"
            val request = createMockRequest(
                xForwardedFor = expectedIp,
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }

        @Test
        fun `should extract localhost IPv6 address`() {
            // Arrange
            val expectedIp = "::1"
            val request = createMockRequest(
                xForwardedFor = expectedIp,
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }
    }

    @Nested
    inner class `Security - Invalid IP Validation` {

        @Test
        fun `should reject X-Forwarded-For with hostname to prevent DNS resolution attack`() {
            // Arrange
            val request = createMockRequest(
                xForwardedFor = "attacker.com",
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe "192.168.1.1" // Falls back to remote address
        }

        @Test
        fun `should reject X-Real-IP with hostname to prevent DNS resolution attack`() {
            // Arrange
            val request = createMockRequest(
                xForwardedFor = null,
                xRealIp = "evil.example.com",
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe "192.168.1.1" // Falls back to remote address
        }

        @Test
        fun `should reject IP with SQL injection attempt`() {
            // Arrange
            val request = createMockRequest(
                xForwardedFor = "'; DROP TABLE users; --",
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe "192.168.1.1" // Falls back to remote address
        }

        @Test
        fun `should reject IP with XSS attempt`() {
            // Arrange
            val request = createMockRequest(
                xForwardedFor = "<script>alert('xss')</script>",
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe "192.168.1.1" // Falls back to remote address
        }

        @Test
        fun `should reject IP with path traversal attempt`() {
            // Arrange
            val request = createMockRequest(
                xForwardedFor = "../../etc/passwd",
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe "192.168.1.1" // Falls back to remote address
        }

        @Test
        fun `should reject malformed IPv4 address`() {
            // Arrange
            val request = createMockRequest(
                xForwardedFor = "999.999.999.999",
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe "192.168.1.1" // Falls back to remote address
        }

        @Test
        fun `should reject IPv4 with too few octets`() {
            // Arrange
            val request = createMockRequest(
                xForwardedFor = "192.168.1.1",
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe "192.168.1.1" // Falls back to remote address
        }

        @Test
        fun `should reject IPv4 with too many octets`() {
            // Arrange
            val request = createMockRequest(
                xForwardedFor = "192.168.1.1.1",
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe "192.168.1.1" // Falls back to remote address
        }

        @Test
        fun `should reject malformed IPv6 address`() {
            // Arrange
            val request = createMockRequest(
                xForwardedFor = "gggg:0db8:85a3::8a2e:0370:7334",
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe "192.168.1.1" // Falls back to remote address
        }

        @Test
        fun `should fallback to X-Real-IP when X-Forwarded-For is invalid`() {
            // Arrange
            val expectedIp = "203.0.113.45"
            val request = createMockRequest(
                xForwardedFor = "invalid.hostname.com",
                xRealIp = expectedIp,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }

        @Test
        fun `should fallback to remote address when both headers are invalid`() {
            // Arrange
            val expectedIp = "192.168.1.1"
            val request = createMockRequest(
                xForwardedFor = "attacker.com",
                xRealIp = "evil.example.com",
                remoteAddress = expectedIp,
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }
    }

    @Nested
    inner class `IP Validation Function` {

        @Test
        fun `should validate standard IPv4 address`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("192.168.1.1") shouldBe true
        }

        @Test
        fun `should validate public IPv4 address`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("8.8.8.8") shouldBe true
        }

        @Test
        fun `should validate IPv4 loopback address`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("127.0.0.1") shouldBe true
        }

        @Test
        fun `should validate IPv4 zero address`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("0.0.0.0") shouldBe true
        }

        @Test
        fun `should validate IPv4 broadcast address`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("255.255.255.255") shouldBe true
        }

        @Test
        fun `should validate full IPv6 address`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("2001:0db8:85a3:0000:0000:8a2e:0370:7334") shouldBe true
        }

        @Test
        fun `should validate compressed IPv6 address`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("2001:db8:85a3::8a2e:370:7334") shouldBe true
        }

        @Test
        fun `should validate fully compressed IPv6 address`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("2001:db8::1") shouldBe true
        }

        @Test
        fun `should validate IPv6 loopback address`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("::1") shouldBe true
        }

        @Test
        fun `should validate IPv6 zero address`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("::") shouldBe true
        }

        @Test
        fun `should validate IPv6 with mixed notation`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("::ffff:192.0.2.1") shouldBe true
        }

        @Test
        fun `should reject hostname`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("example.com") shouldBe false
        }

        @Test
        fun `should reject URL`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("https://example.com") shouldBe false
        }

        @Test
        fun `should reject empty string`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("") shouldBe false
        }

        @Test
        fun `should reject whitespace only`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("   ") shouldBe false
        }

        @Test
        fun `should reject special characters`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("192.168.1.1@attacker.com") shouldBe false
        }

        @Test
        fun `should reject IPv4 with letters`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("192.168.1.x") shouldBe false
        }

        @Test
        fun `should reject IPv4 out of range`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("256.256.256.256") shouldBe false
        }

        @Test
        fun `should reject IPv6 with invalid characters`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("gggg:0db8:85a3::8a2e:0370:7334") shouldBe false
        }

        @Test
        fun `should reject SQL injection in IP format`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("'; DROP TABLE users; --") shouldBe false
        }

        @Test
        fun `should reject XSS payload`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("<script>alert('xss')</script>") shouldBe false
        }

        @Test
        fun `should reject path traversal`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("../../etc/passwd") shouldBe false
        }

        @Test
        fun `should reject CRLF injection`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("192.168.1.1\r\nMalicious-Header: value") shouldBe false
        }

        @Test
        fun `should reject null bytes`() {
            // Act & Assert
            ClientIpExtractor.isValidIp("192.168.1.1\u0000") shouldBe false
        }
    }

    @Nested
    inner class `Edge Cases` {

        @Test
        fun `should handle X-Forwarded-For with single IP and trailing comma`() {
            // Arrange
            val expectedIp = "203.0.113.45"
            val request = createMockRequest(
                xForwardedFor = "$expectedIp,",
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }

        @Test
        fun `should handle X-Forwarded-For with excessive whitespace`() {
            // Arrange
            val expectedIp = "203.0.113.45"
            val request = createMockRequest(
                xForwardedFor = "  $expectedIp  ,  10.0.0.1  ,  172.16.0.1  ",
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }

        @Test
        fun `should prioritize X-Forwarded-For over X-Real-IP when both present`() {
            // Arrange
            val expectedIp = "203.0.113.45"
            val request = createMockRequest(
                xForwardedFor = expectedIp,
                xRealIp = "198.51.100.1",
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }

        @Test
        fun `should handle uppercase hex digits in IPv6`() {
            // Arrange
            val expectedIp = "2001:0DB8:85A3:0000:0000:8A2E:0370:7334"
            val request = createMockRequest(
                xForwardedFor = expectedIp,
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }

        @Test
        fun `should handle mixed case hex digits in IPv6`() {
            // Arrange
            val expectedIp = "2001:0dB8:85a3::8A2e:0370:7334"
            val request = createMockRequest(
                xForwardedFor = expectedIp,
                xRealIp = null,
                remoteAddress = "192.168.1.1",
            )

            // Act
            val result = ClientIpExtractor.extract(request)

            // Assert
            result shouldBe expectedIp
        }
    }

    // Helper function to create mock ServerHttpRequest
    private fun createMockRequest(
        xForwardedFor: String?,
        xRealIp: String?,
        remoteAddress: String?,
    ): ServerHttpRequest {
        val request = mockk<ServerHttpRequest>(relaxed = true)
        val headers = mockk<HttpHeaders>(relaxed = true)

        every { headers.getFirst("X-Forwarded-For") } returns xForwardedFor
        every { headers.getFirst("X-Real-IP") } returns xRealIp
        every { request.headers } returns headers

        if (remoteAddress != null) {
            val inetSocketAddress = InetSocketAddress.createUnresolved(remoteAddress, 8080)
            every { request.remoteAddress } returns inetSocketAddress
            every { request.remoteAddress?.address?.hostAddress } returns remoteAddress
        } else {
            every { request.remoteAddress } returns null
        }

        return request
    }
}
