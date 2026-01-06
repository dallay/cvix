package com.cvix.contact.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.contact.application.send.SendContactCommand
import com.cvix.contact.domain.CaptchaValidationException
import com.cvix.contact.domain.ContactNotificationException
import com.cvix.contact.infrastructure.http.request.SendContactRequest
import com.cvix.contact.infrastructure.http.response.SendContactApiResponse
import com.cvix.contact.infrastructure.http.response.SendContactResponse
import com.cvix.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*
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
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/**
 * Controller for handling contact form submissions.
 *
 * This controller receives contact form data from the frontend, validates the hCaptcha token
 * server-side, and forwards the data to n8n webhook with proper authentication.
 *
 * Uses header-based API versioning (API-Version: v1).
 *
 * ## Security Features:
 * - Server-side hCaptcha validation
 * - API keys kept secure on backend (never exposed to client)
 * - IP address extraction for rate limiting and abuse prevention
 * - Input validation with Jakarta Bean Validation
 *
 * @property mediator The mediator for dispatching commands.
 * @property messageSource The message source for localized messages.
 */
@Validated
@RestController
@RequestMapping(value = ["/api/contact"])
@Tag(name = "Contact", description = "Contact form endpoints")
class ContactController(
    private val mediator: Mediator,
    private val messageSource: MessageSource,
) : ApiController(mediator) {

    /**
     * Endpoint that accepts contact form submissions, validates hCaptcha, dispatches the send command,
     * and returns a localized response.
     *
     * Builds request metadata and determines response language from the Accept-Language header before dispatching.
     *
     * @param request The validated contact form payload including name, email, subject, message, and hCaptcha token.
     * @param serverRequest The server HTTP request used to extract client IP and headers (User-Agent, Referer).
     * @param acceptLanguage Optional Accept-Language header value used to select the response language
     *                      (defaults to "en").
     * @return ResponseEntity containing a SendContactApiResponse with success state and a localized message.
     * @throws ResponseStatusException with HTTP 400 when hCaptcha validation fails.
     * @throws ResponseStatusException with HTTP 500 when notification delivery fails.
     */
    @Operation(
        summary = "Send contact form",
        description = "Submit a contact form message with hCaptcha verification",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Message sent successfully",
                content = [Content(schema = Schema(implementation = SendContactResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data or hCaptcha validation failed",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
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
    suspend fun send(
        @Valid @RequestBody request: SendContactRequest,
        serverRequest: ServerHttpRequest,
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String?
    ): ResponseEntity<SendContactApiResponse> {
        logger.info(
            "Contact form submission from: {}, subject: {}",
            request.email,
            request.subject,
        )

        // Extract client IP for hCaptcha validation and rate limiting
        val clientIp = extractClientIp(serverRequest)

        // Extract metadata
        val metadata = mapOf(
            "userAgent" to (serverRequest.headers.getFirst("User-Agent") ?: "unknown"),
            "referer" to (serverRequest.headers.getFirst("Referer") ?: "direct"),
        )

        // Determine language from Accept-Language header (defaults to 'en')
        val language = parseAcceptLanguage(acceptLanguage)

        // Create and dispatch command
        val command = SendContactCommand(
            id = UUID.randomUUID(),
            name = request.name,
            email = request.email,
            subject = request.subject,
            message = request.message,
            hcaptchaToken = request.hcaptchaToken,
            ipAddress = clientIp,
            metadata = metadata,
        )

        try {
            dispatch(command)

            logger.info("Successfully processed contact form submission from: {}", request.email)

            // Return success response with localized message
            val response = SendContactResponse(
                success = true,
                message = getLocalizedMessage(language, "success"),
            )

            return ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: CaptchaValidationException) {
            logger.warn(
                "hCaptcha validation failed for submission from: {}, IP: {}, reason: {}",
                request.email,
                clientIp,
                e.message,
            )
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                getLocalizedMessage(language, "captcha.invalid"),
                e,
            )
        } catch (e: ContactNotificationException) {
            logger.error("Failed to send contact notification from: {}", request.email, e)
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                getLocalizedMessage(language, "error"),
            )
        }
    }

    /**
     * Determines the client's IP address from the request, preferring forwarded headers and falling
     * back to the remote address.
     *
     * @param request the server HTTP request to inspect
     * @return the client's IP address if determined and valid, or "unknown" if it cannot be determined
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
     * Checks whether the provided string is a valid IPv4 or IPv6 address.
     *
     * @param ip The IP address string to validate.
     * @return `true` if `ip` represents a valid IPv4 or IPv6 address, `false` otherwise.
     */
    @Suppress("SwallowedException")
    private fun isValidIp(ip: String): Boolean {
        return try {
            InetAddress.getByName(ip)
            true
        } catch (e: UnknownHostException) {
            false
        }
    }

    /**
     * Determines the response language from an Accept-Language header value.
     *
     * @param acceptLanguage the value of the Accept-Language header, or null/blank to use the default
     * @return `"es"` if the header's primary language is Spanish, otherwise `"en"`
     */
    private fun parseAcceptLanguage(acceptLanguage: String?): String {
        if (acceptLanguage.isNullOrBlank()) return LANG_ENGLISH

        // Accept-Language format: "en-US,en;q=0.9,es;q=0.8"
        val primaryLang = acceptLanguage.split(",")
            .firstOrNull()
            ?.split("-", ";")
            ?.firstOrNull()
            ?.trim()
            ?.lowercase()

        return if (primaryLang == "es") LANG_SPANISH else LANG_ENGLISH
    }

    /**
     * Gets localized message based on language and message type using MessageSource.
     *
     * @param language The language code (en or es).
     * @param messageType The type of message (success, captcha.invalid, error).
     * @return The localized message string from the message properties files.
     */
    private fun getLocalizedMessage(language: String, messageType: String): String {
        val locale = if (language == LANG_SPANISH) java.util.Locale.forLanguageTag("es") else java.util.Locale.ENGLISH
        val messageKey = "contact.$messageType"
        return try {
            messageSource.getMessage(messageKey, null, locale)
        } catch (e: NoSuchMessageException) {
            logger.warn("Message key not found: $messageKey for locale: $locale", e)
            "Unknown error"
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ContactController::class.java)
        private const val LANG_ENGLISH = "en"
        private const val LANG_SPANISH = "es"
    }
}
