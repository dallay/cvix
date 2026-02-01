package com.cvix.form.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.form.infrastructure.http.request.CreateSubscriberFormRequest
import com.cvix.spring.boot.ApiController
import com.cvix.spring.boot.presentation.MessageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.net.URI
import java.util.*
import org.springframework.context.MessageSource
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for creating subscription forms.
 *
 * This endpoint handles the creation of a new subscription form configuration
 * within the context of the current workspace. It provides full customization
 * of form styling, content, and behavior settings.
 *
 * The controller validates the incoming request payload, resolves workspace and
 * user context from authentication and headers, generates a new form ID, and
 * dispatches the command to the application layer for processing.
 *
 * ## Security
 * - Requires JWT authentication (`bearerAuth`)
 * - Requires valid `X-Workspace-Id` header matching user's workspace access
 * - User must have permission to create forms in the workspace
 *
 * ## Side Effects
 * - Creates a new subscription form entity in the database
 * - Generates domain events (SubscriptionFormCreatedEvent) for downstream processing
 * - Returns HTTP 201 with Location header pointing to the new resource
 *
 * @property mediator Command/query dispatcher for CQRS operations.
 * @property messageSource Resolves localized response messages.
 * @created 25/1/26
 */
@Validated
@RestController
@RequestMapping(value = ["/api/subscription-forms"])
@Tag(name = "Subscription Form", description = "Operations for managing subscription form configurations")
class CreateSubscriptionFormController(
    mediator: Mediator,
    private val messageSource: MessageSource,
) : ApiController(mediator) {

    @Operation(
        summary = "Create a subscription form",
        description = "Creates a new subscription form configuration for the current workspace.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "X-Workspace-Id",
                description = "The ID of the workspace",
                required = true,
                `in` = ParameterIn.HEADER,
                schema = Schema(type = "string", format = "uuid"),
            ),
        ],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Form created successfully",
                content = [Content(schema = Schema(implementation = MessageResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - User lacks access to the workspace",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "409",
                description = "Conflict - Form with this ID already exists",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "429",
                description = "Too Many Requests",
                headers = [
                    Header(
                        name = "Retry-After", description = "Time in seconds to wait before retrying",
                        schema = Schema(type = "integer"),
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
    /**
     * Creates a new subscription form for the current workspace.
     *
     * This endpoint performs the following operations:
     * 1. Validates the incoming request payload against defined constraints (e.g., field lengths, hex color patterns)
     * 2. Extracts workspace ID from the X-Workspace-Id header via context
     * 3. Extracts user ID from the JWT authentication token
     * 4. Generates a new UUID for the subscription form
     * 5. Transforms the request DTO into a CreateSubscriberFormCommand
     * 6. Dispatches the command to the application layer via the mediator
     * 7. Returns a 201 Created response with Location header
     *
     * ## Input Validation
     * - `name`: Required, max 120 characters
     * - `description`: Required, max 500 characters
     * - `successActionType`: Must be "SHOW_MESSAGE" or "REDIRECT"
     * - Color fields: Must be valid hex codes (e.g., #FFFFFF)
     * - Numeric fields: Must be within defined ranges (e.g., padding: 0-100)
     * - Cross-field validation: `redirectUrl` required when `successActionType` is "REDIRECT"
     *
     * ## Business Logic Flow
     * - Command is dispatched asynchronously to the application layer
     * - Application layer creates domain entity and persists it
     * - Domain events (SubscriptionFormCreatedEvent) are recorded and published
     * - Transaction is committed atomically
     *
     * ## Response Behavior
     * - **Success (201)**: Form created, Location header points to `/api/subscription-forms/{id}`
     * - **Validation Error (400)**: Invalid payload, returns ProblemDetail with field errors
     * - **Unauthorized (401)**: Missing or invalid JWT token
     * - **Forbidden (403)**: User lacks access to the specified workspace
     * - **Conflict (409)**: Form with generated ID already exists (rare, retry recommended)
     * - **Rate Limit (429)**: Too many requests, client should back off
     * - **Server Error (500)**: Unexpected error, check logs
     *
     * ## Special Considerations
     * - Form ID is generated server-side to prevent client-supplied ID conflicts
     * - Workspace ID must match the authenticated user's accessible workspaces
     * - Localized success message is returned based on Accept-Language header
     * - All operations are idempotent at the command level (duplicate commands are ignored)
     *
     * @param request The validated request payload containing form configuration, styling, and content settings.
     * @param serverRequest The HTTP request used for extracting locale for message localization.
     * @return A 201 Created response with a localized success message and Location header.
     */
    suspend fun create(
        @Valid @RequestBody request: CreateSubscriberFormRequest,
        serverRequest: ServerHttpRequest,
    ): ResponseEntity<MessageResponse> {
        val workspaceId = workspaceIdFromContext()
        val userId = userIdFromToken()
        val id = UUID.randomUUID()

        val command = request.toCommand(id, workspaceId, userId)
        dispatch(command)

        return ResponseEntity.created(URI.create("/api/subscription-forms/$id")).body(
            MessageResponse(getLocalizedMessage("subscription-form.create.success", serverRequest)),
        )
    }

    private fun getLocalizedMessage(key: String, request: ServerHttpRequest): String {
        val locale = request.headers.acceptLanguageAsLocales.firstOrNull() ?: Locale.ENGLISH
        return messageSource.getMessage(key, null, locale)
    }
}
