package com.cvix.waitlist.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.spring.boot.ApiController
import com.cvix.waitlist.application.create.JoinWaitlistCommand
import com.cvix.waitlist.infrastructure.http.request.JoinWaitlistRequest
import com.cvix.waitlist.infrastructure.http.response.JoinWaitlistApiResponse
import com.cvix.waitlist.infrastructure.http.response.JoinWaitlistResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.Locale
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for managing waitlist operations.
 *
 * This controller handles HTTP requests for joining the waitlist.
 * Uses header-based API versioning (API-Version: v1).
 *
 * @property mediator The mediator for dispatching commands.
 * @property messageSource The message source for localized messages.
 */
@Validated
@RestController
@RequestMapping(value = ["/api/waitlist"])
@Tag(name = "Waitlist", description = "Waitlist management endpoints")
class WaitlistController(
    private val mediator: Mediator,
    private val messageSource: MessageSource,
) : ApiController(mediator) {

    /**
     * Endpoint for joining the waitlist.
     *
     * Accepts user email, source, and language preference to add them to the waitlist.
     * Uses header-based versioning with API-Version header (currently accepts v1).
     *
     * @param request The request body containing email, source, and language.
     * @param serverRequest The server request for extracting client IP.
     * @return ResponseEntity with success or error response.
     */
    @Operation(
        summary = "Join the waitlist",
        description = "Add a user's email to the waitlist for early access notifications",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Successfully added to waitlist",
                content = [Content(schema = Schema(implementation = JoinWaitlistResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "409",
                description = "Email already exists in waitlist",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
                headers = [Header(name = "Retry-After", description = "Seconds until rate limit resets")],
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
        ],
    )
    @PostMapping(
        produces = ["application/vnd.api.v1+json"],
        consumes = ["application/json"],
    )
    suspend fun join(
        @Valid @RequestBody request: JoinWaitlistRequest,
        serverRequest: ServerHttpRequest
    ): ResponseEntity<JoinWaitlistApiResponse> {
        logger.info("Join waitlist request from source: {}, language: {}", request.source, request.language)

        // Extract client IP
        val clientIp = extractClientIp(serverRequest)

        // Extract metadata
        val metadata = mapOf(
            "userAgent" to (serverRequest.headers.getFirst("User-Agent") ?: "unknown"),
            "referer" to (serverRequest.headers.getFirst("Referer") ?: "direct"),
        )

        // Dispatch command
        val command = JoinWaitlistCommand(
            id = java.util.UUID.randomUUID(),
            email = request.email,
            source = request.source,
            language = request.language,
            ipAddress = clientIp,
            metadata = metadata,
        )

        dispatch(command)

        logger.info("Successfully added email to waitlist")

        // Return success response
        val response = JoinWaitlistResponse(
            success = true,
            message = getLocalizedMessage(request.language, "success"),
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Extracts the client's real IP address from the request.
     *
     * Checks X-Forwarded-For, X-Real-IP headers (for proxies/load balancers),
     * validates the format, and falls back to remote address.
     * Only returns header values if they pass IP format validation.
     *
     * @param request The server HTTP request.
     * @return The validated client's IP address, or "unknown" if unable to determine.
     */
    private fun extractClientIp(request: ServerHttpRequest): String {
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
     * Security: Pre-filters to prevent DNS resolution of hostnames, which could lead to
     * DoS attacks via thread exhaustion in reactive WebFlux context.
     *
     * @param ip The IP address string to validate.
     * @return true if the string is a valid IP address format, false otherwise.
     */
    @Suppress("SwallowedException")
    private fun isValidIp(ip: String): Boolean {
        // Pre-filter: IP addresses only contain hex digits, dots, and colons
        // This prevents DNS resolution for hostnames (e.g., "attacker.com")
        if (!ip.matches(Regex("^[0-9a-fA-F:.]+$"))) {
            return false
        }

        return try {
            val addr = InetAddress.getByName(ip)
            // Verify no reverse DNS occurred (hostAddress should match input)
            ip == addr.hostAddress
        } catch (e: UnknownHostException) {
            false
        }
    }

    /**
     * Gets localized message based on language and message type using MessageSource.
     *
     * @param language The language code (en or es).
     * @param messageType The type of message (success, duplicate, invalid, error).
     * @return The localized message string from the message properties files.
     */
    private fun getLocalizedMessage(language: String, messageType: String): String {
        val locale = if (language == LANG_SPANISH) Locale.forLanguageTag("es") else Locale.ENGLISH
        val messageKey = "waitlist.$messageType"
        return try {
            messageSource.getMessage(messageKey, null, locale)
        } catch (e: NoSuchMessageException) {
            logger.warn("Message key not found: $messageKey for locale: $locale", e)
            "Unknown error"
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WaitlistController::class.java)
        private const val LANG_SPANISH = "es"
    }
}
