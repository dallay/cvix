package com.cvix.waitlist.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.spring.boot.ApiController
import com.cvix.waitlist.application.create.JoinWaitlistCommand
import com.cvix.waitlist.domain.EmailAlreadyExistsException
import com.cvix.waitlist.infrastructure.http.request.JoinWaitlistRequest
import com.cvix.waitlist.infrastructure.http.response.JoinWaitlistApiResponse
import com.cvix.waitlist.infrastructure.http.response.JoinWaitlistResponse
import com.cvix.waitlist.infrastructure.http.response.WaitlistErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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
 */
@RestController
@RequestMapping(value = ["/api/waitlist"])
@Tag(name = "Waitlist", description = "Waitlist management endpoints")
@Suppress("TooGenericExceptionCaught")
class WaitlistController(
    private val mediator: Mediator,
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
                content = [Content(schema = Schema(implementation = WaitlistErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "409",
                description = "Email already exists in waitlist",
                content = [Content(schema = Schema(implementation = WaitlistErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
                headers = [Header(name = "Retry-After", description = "Seconds until rate limit resets")],
                content = [Content(schema = Schema(implementation = WaitlistErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = WaitlistErrorResponse::class))],
            ),
        ],
    )
    @PostMapping(
        produces = ["application/vnd.api.v1+json"],
        consumes = ["application/json"],
    )
    suspend fun join(
        @Validated @RequestBody request: JoinWaitlistRequest,
        serverRequest: ServerHttpRequest
    ): ResponseEntity<JoinWaitlistApiResponse> {
        logger.info("Join waitlist request from source: {}, language: {}", request.source, request.language)

        try {
            // Extract client IP
            val clientIp = extractClientIp(serverRequest)

            // Extract metadata
            val metadata = mapOf(
                "userAgent" to (serverRequest.headers.getFirst("User-Agent") ?: "unknown"),
                "referer" to (serverRequest.headers.getFirst("Referer") ?: "direct"),
            )

            // Dispatch command
            val command = JoinWaitlistCommand(
                id = UUID.randomUUID(),
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
                message = getLocalizedMessage(request.language, MSG_TYPE_SUCCESS),
            )

            return ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: EmailAlreadyExistsException) {
            logger.warn("Email already exists in waitlist: {}", e.message)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                WaitlistErrorResponse(
                    error = "EMAIL_ALREADY_EXISTS",
                    message = getLocalizedMessage(request.language, MSG_TYPE_DUPLICATE),
                ),
            )
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request data: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                WaitlistErrorResponse(
                    error = "INVALID_REQUEST",
                    message = getLocalizedMessage(request.language, MSG_TYPE_INVALID),
                ),
            )
        } catch (e: RuntimeException) {
            logger.error("Unexpected error joining waitlist", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                WaitlistErrorResponse(
                    error = "INTERNAL_ERROR",
                    message = getLocalizedMessage(request.language, MSG_TYPE_ERROR),
                ),
            )
        }
    }

    /**
     * Extracts the client's real IP address from the request.
     *
     * Checks X-Forwarded-For, X-Real-IP headers (for proxies/load balancers),
     * and falls back to remote address.
     *
     * @param request The server HTTP request.
     * @return The client's IP address.
     */
    private fun extractClientIp(request: ServerHttpRequest): String {
        val xForwardedFor = request.headers.getFirst("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",").first().trim()
        }

        val xRealIp = request.headers.getFirst("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp.trim()
        }

        // Fallback to remote address
        return request.remoteAddress?.address?.hostAddress ?: "unknown"
    }

    /**
     * Gets localized message based on language and message type.
     *
     * @param language The language code (en or es).
     * @param messageType The type of message (success, duplicate, invalid, error).
     * @return The localized message string.
     */
    private fun getLocalizedMessage(language: String, messageType: String): String {
        return if (language == LANG_SPANISH) {
            when (messageType) {
                MSG_TYPE_SUCCESS -> "¡Te has unido a la lista de espera!"
                MSG_TYPE_DUPLICATE -> "Este correo ya está en la lista de espera"
                MSG_TYPE_INVALID -> "Datos de solicitud inválidos"
                MSG_TYPE_ERROR -> "Error interno del servidor. Por favor, intenta de nuevo más tarde."
                else -> "Error desconocido"
            }
        } else {
            when (messageType) {
                MSG_TYPE_SUCCESS -> "You've been added to the waitlist!"
                MSG_TYPE_DUPLICATE -> "This email is already on the waitlist"
                MSG_TYPE_INVALID -> "Invalid request data"
                MSG_TYPE_ERROR -> "Internal server error. Please try again later."
                else -> "Unknown error"
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WaitlistController::class.java)
        private const val MSG_TYPE_SUCCESS = "success"
        private const val MSG_TYPE_DUPLICATE = "duplicate"
        private const val MSG_TYPE_INVALID = "invalid"
        private const val MSG_TYPE_ERROR = "error"
        private const val LANG_SPANISH = "es"
    }
}
