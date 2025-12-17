package com.cvix.common.domain.security

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

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

    /**
     * Computes HMAC-SHA256 of the input string using the provided secret key.
     *
     * @param input The input string to hash.
     * @param secretKey The secret key for HMAC.
     * @param algorithm The HMAC algorithm to use (default is "HmacSHA256").
     * @return The hexadecimal representation of the HMAC.
     */
    fun hmacSha256(input: String, secretKey: String, algorithm: String = "HmacSHA256"): String {
        val mac = Mac.getInstance(algorithm)
        val keySpec = SecretKeySpec(secretKey.toByteArray(), algorithm)
        mac.init(keySpec)
        val hmacBytes = mac.doFinal(input.toByteArray())
        return hmacBytes.joinToString("") { "%02x".format(it) }
    }
}
