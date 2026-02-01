package com.cvix.form.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.common.domain.presentation.pagination.CursorPageResponse
import com.cvix.form.application.SubscriberFormResponse
import com.cvix.form.application.search.SearchSubscriberFormsQuery
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
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for searching subscription forms.
 *
 * This endpoint allows listing and filtering subscription forms within a workspace.
 */
@Validated
@RestController
@RequestMapping(value = ["/api/subscription-forms"])
@Tag(name = "Subscription Form")
class SearchSubscriptionFormsController(
    mediator: Mediator,
) : ApiController(mediator) {

    @Operation(
        summary = "Search subscription forms",
        description = "Searches for subscription forms in the current workspace with pagination and filters.",
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
                responseCode = "200",
                description = "Search completed successfully",
                content = [Content(schema = Schema(implementation = CursorPageResponse::class))],
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
    @GetMapping(produces = ["application/vnd.api.v1+json"])
    suspend fun search(
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) cursor: String?,
        @Suppress("UnusedParameter") serverRequest: ServerHttpRequest,
    ): ResponseEntity<CursorPageResponse<SubscriberFormResponse>> {
        val workspaceId = workspaceIdFromContext()
        val userId = userIdFromToken()

        val query = SearchSubscriberFormsQuery(
            workspaceId = workspaceId,
            userId = userId,
            criteria = null,
            size = size,
            cursor = cursor,
            sort = null,
        )
        val response = ask(query)

        return ResponseEntity.ok(response)
    }
}
