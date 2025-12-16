package com.cvix.common.domain.security

import java.security.MessageDigest

object HashUtils {
    /**
     * Hashes the input string using SHA-256 and returns the hexadecimal representation of the hash.
     *
     * @param input The input string to hash.
     * @param algorithm The hashing algorithm to use (default is "SHA-256").
     * @return The hexadecimal representation of the hash.
     */
    fun hashSha256(input: String, algorithm: String = "SHA-256"): String {
        val digest = MessageDigest.getInstance(algorithm)
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
