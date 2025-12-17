package com.cvix.common.domain.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Unit tests for HashUtils cryptographic functions.
 *
 * These tests verify:
 * - SHA-256 hashing correctness
 * - HMAC-SHA256 correctness and security properties
 * - Deterministic behavior (same input = same output)
 * - Different inputs produce different outputs
 * - Hex encoding format
 * - Rainbow table attack resistance (HMAC with secret)
 */
class HashUtilsTest {

    @Test
    fun `hashSha256 should produce deterministic output`() {
        // Arrange
        val input = "test-input"

        // Act
        val hash1 = HashUtils.hashSha256(input)
        val hash2 = HashUtils.hashSha256(input)

        // Assert
        assertThat(hash1).isEqualTo(hash2)
    }

    @Test
    fun `hashSha256 should produce different hashes for different inputs`() {
        // Arrange
        val input1 = "test-input-1"
        val input2 = "test-input-2"

        // Act
        val hash1 = HashUtils.hashSha256(input1)
        val hash2 = HashUtils.hashSha256(input2)

        // Assert
        assertThat(hash1).isNotEqualTo(hash2)
    }

    @Test
    fun `hashSha256 should produce 64-character hexadecimal string`() {
        // Arrange
        val input = "test-input"

        // Act
        val hash = HashUtils.hashSha256(input)

        // Assert
        assertThat(hash).hasSize(64)
        assertThat(hash).matches("^[0-9a-f]{64}$")
    }

    @Test
    fun `hashSha256 should produce known hash for known input`() {
        // Arrange
        val input = "hello"
        // Expected SHA-256 hash of "hello"
        val expectedHash = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"

        // Act
        val hash = HashUtils.hashSha256(input)

        // Assert
        assertThat(hash).isEqualTo(expectedHash)
    }

    @Test
    fun `hashSha256 should handle empty string`() {
        // Arrange
        val input = ""
        // Expected SHA-256 hash of empty string
        val expectedHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"

        // Act
        val hash = HashUtils.hashSha256(input)

        // Assert
        assertThat(hash).isEqualTo(expectedHash)
    }

    @Test
    fun `hmacSha256 should produce deterministic output with same secret`() {
        // Arrange
        val input = "test-input"
        val secret = "test-secret"

        // Act
        val hmac1 = HashUtils.hmacSha256(input, secret)
        val hmac2 = HashUtils.hmacSha256(input, secret)

        // Assert
        assertThat(hmac1).isEqualTo(hmac2)
    }

    @Test
    fun `hmacSha256 should produce different outputs for different secrets`() {
        // Arrange
        val input = "test-input"
        val secret1 = "test-secret-1"
        val secret2 = "test-secret-2"

        // Act
        val hmac1 = HashUtils.hmacSha256(input, secret1)
        val hmac2 = HashUtils.hmacSha256(input, secret2)

        // Assert - different secrets should produce different HMACs
        assertThat(hmac1).isNotEqualTo(hmac2)
    }

    @Test
    fun `hmacSha256 should produce different outputs for different inputs`() {
        // Arrange
        val input1 = "test-input-1"
        val input2 = "test-input-2"
        val secret = "test-secret"

        // Act
        val hmac1 = HashUtils.hmacSha256(input1, secret)
        val hmac2 = HashUtils.hmacSha256(input2, secret)

        // Assert
        assertThat(hmac1).isNotEqualTo(hmac2)
    }

    @Test
    fun `hmacSha256 should produce 64-character hexadecimal string`() {
        // Arrange
        val input = "test-input"
        val secret = "test-secret"

        // Act
        val hmac = HashUtils.hmacSha256(input, secret)

        // Assert
        assertThat(hmac).hasSize(64)
        assertThat(hmac).matches("^[0-9a-f]{64}$")
    }

    @Test
    fun `hmacSha256 should produce known HMAC for known input and secret`() {
        // Arrange
        val input = "hello"
        val secret = "secret"
        // Expected HMAC-SHA256 of "hello" with secret "secret"
        val expectedHmac = "88aab3ede8d3adf94d26ab90d3bafd4a2083070c3bcce9c014ee04a443847c0b"

        // Act
        val hmac = HashUtils.hmacSha256(input, secret)

        // Assert
        assertThat(hmac).isEqualTo(expectedHmac)
    }

    @Test
    fun `hmacSha256 should handle empty input`() {
        // Arrange
        val input = ""
        val secret = "secret"
        // Expected HMAC-SHA256 of empty string with secret "secret"
        val expectedHmac = "f9e66e179b6747ae54108f82f8ade8b3c25d76fd30afde6c395822c530196169"

        // Act
        val hmac = HashUtils.hmacSha256(input, secret)

        // Assert
        assertThat(hmac).isEqualTo(expectedHmac)
    }

    @Test
    fun `hmacSha256 should throw exception for empty secret`() {
        // Arrange
        val input = "hello"
        val secret = ""

        // Act & Assert
        // Empty secrets are not allowed by the HMAC specification
        // This is correct security behavior - a secret must be provided
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            HashUtils.hmacSha256(input, secret)
        }
    }

    @Test
    fun `hmacSha256 should be resistant to rainbow table attacks`() {
        // Arrange
        // Common IPv4 addresses that might be in a rainbow table
        val commonIp1 = "192.168.1.1"
        val commonIp2 = "192.168.1.1"
        val secret1 = "production-secret-key-2024"
        val secret2 = "different-secret-key-2024"

        // Act
        val hmac1 = HashUtils.hmacSha256(commonIp1, secret1)
        val hmac2 = HashUtils.hmacSha256(commonIp2, secret2)

        // Assert - same IP with different secrets should produce completely different HMACs
        // This demonstrates that even if an attacker has a rainbow table for SHA-256 hashes
        // of IP addresses, they cannot reverse the HMAC without knowing the secret
        assertThat(hmac1).isNotEqualTo(hmac2)

        // The HMAC should look completely random and unrelated
        val sha256Hash = HashUtils.hashSha256(commonIp1)
        assertThat(hmac1).isNotEqualTo(sha256Hash) // HMAC should be different from plain SHA-256
    }

    @Test
    fun `hmacSha256 should handle special characters and unicode`() {
        // Arrange
        val input = "Hello 世界! 🌍 Special: @#$%^&*()"
        val secret = "unicode-secret-密钥"

        // Act
        val hmac = HashUtils.hmacSha256(input, secret)

        // Assert
        assertThat(hmac).hasSize(64)
        assertThat(hmac).matches("^[0-9a-f]{64}$")
    }

    @Test
    fun `hmacSha256 should produce different results than plain SHA-256`() {
        // Arrange
        val input = "192.168.1.1"
        val secret = "my-secret"

        // Act
        val sha256Hash = HashUtils.hashSha256(input)
        val hmacHash = HashUtils.hmacSha256(input, secret)

        // Assert - HMAC should be different from plain SHA-256
        // This is crucial for security: attackers with SHA-256 rainbow tables
        // cannot use them against HMAC-protected values
        assertThat(hmacHash).isNotEqualTo(sha256Hash)
    }

    @Test
    fun `hmacSha256 security demonstration - IP address protection`() {
        // Arrange
        // Simulate IP addresses that an attacker might target
        val ipAddresses = listOf(
            "192.168.1.1",
            "10.0.0.1",
            "172.16.0.1",
            "8.8.8.8",
            "1.1.1.1",
        )
        val productionSecret = "production-hmac-secret-key-2024-keep-this-safe"

        // Act - Hash all IPs with HMAC
        val hmacs = ipAddresses.map { ip -> HashUtils.hmacSha256(ip, productionSecret) }

        // Assert
        // 1. All HMACs should be unique
        assertThat(hmacs.toSet()).hasSize(ipAddresses.size)

        // 2. HMACs should look random and not reveal the original IP
        hmacs.forEach { hmac ->
            assertThat(hmac).hasSize(64)
            assertThat(hmac).matches("^[0-9a-f]{64}$")
        }

        // 3. Without the secret, an attacker cannot determine which HMAC corresponds
        //    to which IP, even if they have a rainbow table of SHA-256(IP) values
        val sha256Hashes = ipAddresses.map { ip -> HashUtils.hashSha256(ip) }
        // HMAC values must not match any plain SHA-256 hashes. Use AssertJ fluent assertion
        // for clearer intent and better failure messages.
        assertThat(sha256Hashes).doesNotContainAnyElementsOf(hmacs)
    }
}
