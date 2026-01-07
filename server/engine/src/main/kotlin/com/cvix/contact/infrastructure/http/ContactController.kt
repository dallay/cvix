package com.cvix.contact.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.contact.application.send.SendContactCommand
import com.cvix.contact.domain.CaptchaValidationException
import com.cvix.contact.domain.ContactNotificationException
import com.cvix.contact.infrastructure.http.request.SendContactRequest
import com.cvix.contact.infrastructure.http.response.SendContactApiResponse
import com.cvix.contact.infrastructure.http.response.SendContactResponse
import com.cvix.spring.boot.ApiController
import com.cvix.spring.boot.infrastructure.http.ClientIpExtractor
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
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
     * Endpoint for sending contact form submissions.
     *
     * Accepts contact form data, validates hCaptcha server-side, and forwards to n8n webhook.
     * Uses header-based versioning with API-Version header (currently accepts v1).
     *
     * @param request The request body containing contact form data and hCaptcha token.
     * @param serverRequest The server request for extracting client IP.
     * @param acceptLanguage Optional Accept-Language header for localized responses.
     * @return ResponseEntity with success or error response.
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
            "Contact form submission received, subject: {}",
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

            logger.info("Successfully processed contact form submission with ID: {}", command.id)

            // Return success response with localized message
            val response = SendContactResponse(
                success = true,
                message = getLocalizedMessage(language, "success"),
            )

            return ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: CaptchaValidationException) {
            logger.warn(
                "hCaptcha validation failed for submission ID: {}, IP: {}, reason: {}",
                command.id,
                clientIp,
                e.message,
            )
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                getLocalizedMessage(language, "captcha.invalid"),
                e,
            )
        } catch (e: ContactNotificationException) {
            logger.error("Failed to send contact notification for submission ID: {}", command.id, e)
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                getLocalizedMessage(language, "error"),
            )
        }
    }

    /**
     * Extracts the client's real IP address from the request.
     *
     * Delegates to shared utility [ClientIpExtractor] for consistent IP extraction
     * across controllers.
     *
     * @param request The server HTTP request.
     * @return The validated client's IP address, or "unknown" if unable to determine.
     */
    private fun extractClientIp(request: ServerHttpRequest): String =
        ClientIpExtractor.extract(request)

    /**
     * Parses the Accept-Language header to determine the preferred language.
     *
     * @param acceptLanguage The Accept-Language header value.
     * @return Language code ("en" or "es"), defaults to "en".
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
        val locale = if (language == LANG_SPANISH) Locale.forLanguageTag("es") else Locale.ENGLISH
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
