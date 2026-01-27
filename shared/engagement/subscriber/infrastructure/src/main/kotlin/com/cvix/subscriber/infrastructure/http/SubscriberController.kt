package com.cvix.subscriber.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.form.application.find.SubscriberFormFinder
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.spring.boot.ApiController
import com.cvix.spring.boot.infrastructure.http.ClientIpExtractor
import com.cvix.spring.boot.presentation.MessageResponse
import com.cvix.subscriber.application.create.CreateSubscriberCommand
import com.cvix.subscriber.domain.Attributes
import com.cvix.subscriber.infrastructure.http.request.SubscriberRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.net.URI
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/**
 *
 * @created 19/1/26
 */
@Validated
@RestController
@RequestMapping(value = ["/api/subscribers"])
@Tag(name = "Subscriber", description = "Subscriber management endpoints")
class SubscriberController(
    mediator: Mediator,
    private val messageSource: MessageSource,
    private val formFinder: SubscriberFormFinder,
) : ApiController(mediator) {

    @Operation(
        summary = "Subscribe (email capture)",
        description = "Capture a user's email to subscribe them for notifications " +
            "(supports waitlist, newsletter or generic email capture). " +
            "Requires workspace context via the X-Workspace-Id header OR a valid formId in the body.",
        parameters = [
            Parameter(
                name = "X-Workspace-Id",
                description = "The ID of the workspace where the subscriber will be added. " +
                    "Optional if formId is provided in the request body.",
                required = false,
                `in` = ParameterIn.HEADER,
                schema = Schema(type = "string", format = "uuid"),
                example = "123e4567-e89b-12d3-a456-426614174000",
            ),
        ],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Successfully subscribed / email captured",
                content = [Content(schema = Schema(implementation = MessageResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "409",
                description = "Email already subscribed",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
                headers = [
                    Header(
                        name = "Retry-After",
                        description = "Seconds until rate limit resets",
                    ),
                ],
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
    suspend fun subscribe(
        @Valid @RequestBody request: SubscriberRequest,
        serverRequest: ServerHttpRequest
    ): ResponseEntity<MessageResponse> {
        log.debug("Subscribing request from source: {}", request.source)

        val command = toCommand(request, serverRequest)

        dispatch(command).also { log.info("Subscribed successfully with id {}", it) }

        return ResponseEntity.created(
            URI.create("/api/subscribers/${command.id}"),
        ).body(
            MessageResponse(
                message = getLocalizedMessage(serverRequest),
            ),
        )
    }

    private suspend fun toCommand(
        request: SubscriberRequest,
        serverRequest: ServerHttpRequest
    ): CreateSubscriberCommand {
        val correlationId = serverRequest.headers.getFirst("X-Correlation-ID") ?: "unknown"
        val systemMetadata = mapOf(
            "userAgent" to (serverRequest.headers.getFirst("User-Agent") ?: "unknown"),
            "referer" to (serverRequest.headers.getFirst("Referer") ?: "direct"),
            "correlationId" to correlationId,
        )
        val combinedMetadata = systemMetadata + (request.metadata ?: emptyMap())

        val workspaceId = request.formId?.let { formId ->
            formFinder.findById(SubscriptionFormId(formId))?.workspaceId?.value
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Form not found with id: $formId")
        } ?: workspaceIdFromContext()

        return CreateSubscriberCommand(
            id = UUID.randomUUID(),
            email = request.email,
            source = request.source,
            language = request.language,
            ipAddress = ClientIpExtractor.extract(serverRequest),
            attributes = Attributes(metadata = combinedMetadata),
            workspaceId = workspaceId,
        )
    }

    private fun getLocalizedMessage(request: ServerHttpRequest): String {
        val locale = request.headers.acceptLanguageAsLocales.firstOrNull() ?: Locale.ENGLISH
        return messageSource.getMessage("subscriber.subscribe.success", null, locale)
    }

    companion object {
        private val log = LoggerFactory.getLogger(SubscriberController::class.java)
    }
}
