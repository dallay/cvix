package com.cvix.common.domain.security

/**
 * Functional interface for hashing strings.
 *
 * Implementations of this interface provide a hashing algorithm
 * that takes a plain text input and returns its hashed representation.
 */
fun interface Hasher {
    /**
     * Hashes the given input string and returns the hashed value.
     *
     * @param input the plain text string to hash
     * @return the hashed representation of the input
     */
    fun hash(input: String): String
}
