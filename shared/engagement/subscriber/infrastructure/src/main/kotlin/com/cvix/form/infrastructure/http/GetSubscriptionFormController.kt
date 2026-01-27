package com.cvix.form.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.form.application.SubscriberFormResponse
import com.cvix.form.application.details.DetailSubscriberFormQuery
import com.cvix.form.application.find.SubscriberFormFinder
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormStatus
import com.cvix.spring.boot.ApiController
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
import java.util.*
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/**
 * Controller for retrieving subscription form details.
 *
 * This endpoint allows fetching the configuration of a specific subscription form.
 */
@Validated
@RestController
@RequestMapping(value = ["/api/v1/subscription-forms"])
@Tag(name = "Subscription Form")
class GetSubscriptionFormController(
    mediator: Mediator,
    private val formFinder: SubscriberFormFinder,
) : ApiController(mediator) {

    @Operation(
        summary = "Get subscription form details",
        description = "Retrieves the configuration of a specific subscription form. " +
            "Supports both Admin (via X-Workspace-Id header and auth) and Public access (for embeds).",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "X-Workspace-Id",
                description = "The ID of the workspace (required for Admin access)",
                required = false,
                `in` = ParameterIn.HEADER,
                schema = Schema(type = "string", format = "uuid"),
            ),
        ],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Form details retrieved successfully",
                content = [Content(schema = Schema(implementation = SubscriberFormResponse::class))],
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
                responseCode = "404",
                description = "Form not found",
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
    @GetMapping(value = ["/{id}"], produces = ["application/vnd.api.v1+json"])
    suspend fun get(
        @PathVariable id: UUID,
        @Suppress("UnusedParameter") serverRequest: ServerHttpRequest,
    ): ResponseEntity<SubscriberFormResponse> {
        // Try Admin access first
        @Suppress("SwallowedException")
        val workspaceId = try {
            workspaceIdFromContext()
        } catch (e: ResponseStatusException) {
            // Admin context not available, fall back to public access
            null
        }
        @Suppress("SwallowedException")
        val userId = try {
            userIdFromToken()
        } catch (e: ResponseStatusException) {
            // User token not available, fall back to public access
            null
        }

        if (workspaceId != null && userId != null) {
            val query = DetailSubscriberFormQuery(id, workspaceId, userId)
            return ResponseEntity.ok(ask(query))
        }

        // Public access
        val form = formFinder.findById(SubscriptionFormId(id))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Form not found")

        if (form.status != SubscriptionFormStatus.PUBLISHED) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Form is not published")
        }

        return ResponseEntity.ok(SubscriberFormResponse.from(form))
    }
}
