package com.cvix.common.domain.vo.ip

/**
 * Value class representing a SHA-256 hash of an IP address.
 *
 * This class enforces that the provided string is a valid SHA-256 hash,
 * which must be exactly 64 hexadecimal characters.
 *
 * @property value The SHA-256 hash string of the IP address.
 * @throws IllegalArgumentException if the input is not a valid SHA-256 hex string.
 *
 * @constructor Creates an [IpHash] instance after validating the input.
 *
 * @created 20/1/26
 */
@JvmInline
value class IpHash(val value: String) {
    init {
        require(value.length == SHA256_HASH_LENGTH) { "IP hash must be a SHA-256 hash (64 hex characters)" }
        require(value.matches(Regex("^[a-fA-F0-9]{64}$"))) { "IP hash must be a valid SHA-256 hex string" }
    }

    companion object {
        /** The required length of a SHA-256 hash in hexadecimal characters. */
        private const val SHA256_HASH_LENGTH = 64
        fun from(ipHashed: String): IpHash = IpHash(ipHashed)
    }
}
