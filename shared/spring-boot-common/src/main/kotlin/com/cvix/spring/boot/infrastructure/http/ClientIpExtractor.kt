package com.cvix.spring.boot.infrastructure.http

import java.net.InetAddress
import java.net.UnknownHostException
import org.springframework.http.server.reactive.ServerHttpRequest

/**
 * Utility for extracting and validating client IP addresses from HTTP requests.
 *
 * Provides secure IP extraction from request headers (X-Forwarded-For, X-Real-IP)
 * with validation to prevent DNS resolution attacks and ensure data integrity.
 *
 * Security Features:
 * - Pre-filters IP strings to contain only valid IP address characters (hex digits, dots, colons)
 * - Prevents DNS resolution of hostnames which could lead to DoS via thread exhaustion
 * - Handles IPv6 address normalization (e.g., "::1" vs "0:0:0:0:0:0:0:1")
 * - Falls back to direct remote address from connection when headers are missing or invalid
 *
 * Usage:
 * ```kotlin
 * val clientIp = ClientIpExtractor.extract(request)
 * ```
 */
object ClientIpExtractor {

    /**
     * Compiled regex for IP address format validation.
     * Matches only hexadecimal digits, dots (IPv4), and colons (IPv6).
     * Prevents DNS resolution of arbitrary hostnames.
     *
     * Compiled once at class loading for efficiency.
     */
    private val IP_FORMAT_REGEX = Regex("^[0-9a-fA-F:.]+$")

    /**
     * Extracts the client's real IP address from the request.
     *
     * Checks X-Forwarded-For and X-Real-IP headers (typically set by proxies/load balancers),
     * validates the format to prevent DNS attacks, and falls back to the direct remote address.
     *
     * Header Processing:
     * - X-Forwarded-For: Can contain multiple IPs (comma-separated); takes the first valid one
     * - X-Real-IP: Contains a single IP; used if X-Forwarded-For is absent or invalid
     * - Remote Address: Used as final fallback for direct connections
     *
     * @param request The server HTTP request to extract IP from.
     * @return The validated client's IP address, or "unknown" if unable to determine.
     */
    fun extract(request: ServerHttpRequest): String {
        val xForwardedFor = request.headers.getFirst("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            val ip = xForwardedFor.split(",").first().trim()
            if (isValidIp(ip)) {
                return ip
            }
        }

        val xRealIp = request.headers.getFirst("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            val ip = xRealIp.trim()
            if (isValidIp(ip)) {
                return ip
            }
        }

        // Fallback to remote address (already validated by Java networking layer)
        return request.remoteAddress?.address?.hostAddress ?: "unknown"
    }

    /**
     * Validates if a string is a well-formed IP address (IPv4 or IPv6).
     *
     * Two-stage validation:
     * 1. Pre-filter: IP addresses only contain hex digits, dots, and colons
     *    (prevents DNS resolution of hostnames, which could lead to DoS attacks)
     * 2. Format check: Attempts to parse as IP address to verify validity
     *
     * Note: Does not perform strict equality check with hostAddress due to IPv6 normalization.
     * For example, "::1" normalizes to "0:0:0:0:0:0:0:1", which are semantically identical.
     *
     * @param ip The IP address string to validate.
     * @return true if the string is a valid IP address format, false otherwise.
     */
    fun isValidIp(ip: String): Boolean {
        // Pre-filter: IP addresses only contain hex digits, dots, and colons
        // This prevents DNS resolution for hostnames (e.g., "attacker.com")
        if (!ip.matches(IP_FORMAT_REGEX)) {
            return false
        }

        return try {
            // Validate by attempting to parse as IP address
            // If this succeeds, the string is a valid IP (IPv4 or IPv6)
            InetAddress.getByName(ip)
            true
        } catch (@Suppress("SwallowedException") e: UnknownHostException) {
            false
        }
    }
}