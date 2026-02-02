package com.cvix.common.domain.security

import java.security.MessageDigest

/**
 * SHA-256 implementation of [Hasher].
 *
 * This class computes the SHA-256 digest of the provided input string
 * using UTF-8 encoding and returns the result as a lowercase hexadecimal
 * string.
 */
class Sha256Hasher : Hasher {
    /**
     * Hashes the given input string and returns the hashed value as a
     * lowercase hexadecimal representation of the SHA-256 digest.
     *
     * This method uses UTF-8 to encode the input string before hashing.
     *
     * @param input the plain text string to hash
     * @return the SHA-256 hash as a lowercase hex string
     */
    override fun hash(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
