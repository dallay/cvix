package com.cvix.form.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.common.domain.presentation.pagination.CursorPageResponse
import com.cvix.form.application.SubscriberFormResponse
import com.cvix.form.application.delete.DeleteSubscriberFormCommand
import com.cvix.form.application.details.DetailSubscriberFormQuery
import com.cvix.form.application.search.SearchSubscriberFormsQuery
import com.cvix.form.infrastructure.http.request.CreateSubscriberFormRequest
import com.cvix.form.infrastructure.http.request.UpdateSubscriberFormRequest
import com.cvix.spring.boot.ApiController
import com.cvix.spring.boot.presentation.MessageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.net.URI
import java.util.*
import org.springframework.context.MessageSource
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping(value = ["/api/subscription-forms"])
@Tag(name = "Subscription Form", description = "Endpoints for managing subscription forms")
class SubscriptionFormController(
    mediator: Mediator,
    private val messageSource: MessageSource,
) : ApiController(mediator) {

    @Operation(
        summary = "Create a subscription form",
        description = "Creates a new subscription form configuration for the current workspace.",
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
        ],
    )
    @PostMapping(
        produces = ["application/vnd.api.v1+json"],
        consumes = ["application/json"],
    )
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

    @Operation(
        summary = "Update a subscription form",
        description = "Updates an existing subscription form configuration.",
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
    @PutMapping(
        value = ["/{id}"],
        produces = ["application/vnd.api.v1+json"],
        consumes = ["application/json"],
    )
    suspend fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateSubscriberFormRequest,
        serverRequest: ServerHttpRequest,
    ): ResponseEntity<MessageResponse> {
        val workspaceId = workspaceIdFromContext()
        val userId = userIdFromToken()

        val command = request.toCommand(id, workspaceId, userId)
        dispatch(command)

        return ResponseEntity.ok(
            MessageResponse(getLocalizedMessage("subscription-form.update.success", serverRequest)),
        )
    }

    @Operation(
        summary = "Delete a subscription form",
        description = "Deletes an existing subscription form configuration.",
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
    @DeleteMapping(value = ["/{id}"], produces = ["application/vnd.api.v1+json"])
    suspend fun delete(
        @PathVariable id: UUID,
        serverRequest: ServerHttpRequest,
    ): ResponseEntity<MessageResponse> {
        val workspaceId = workspaceIdFromContext()
        val userId = userIdFromToken()

        val command = DeleteSubscriberFormCommand(id, workspaceId, userId)
        dispatch(command)

        return ResponseEntity.ok(
            MessageResponse(getLocalizedMessage("subscription-form.delete.success", serverRequest)),
        )
    }

    @Operation(
        summary = "Get subscription form details",
        description = "Retrieves the configuration of a specific subscription form.",
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
    @GetMapping(value = ["/{id}"], produces = ["application/vnd.api.v1+json"])
    suspend fun get(
        @PathVariable id: UUID,
        @Suppress("UnusedParameter") serverRequest: ServerHttpRequest,
    ): ResponseEntity<SubscriberFormResponse> {
        val workspaceId = workspaceIdFromContext()
        val userId = userIdFromToken()

        val query = DetailSubscriberFormQuery(id, workspaceId, userId)
        val response = ask(query)

        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "Search subscription forms",
        description = "Searches for subscription forms in the current workspace with pagination and filters.",
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

    private fun getLocalizedMessage(key: String, request: ServerHttpRequest): String {
        val locale = request.headers.acceptLanguageAsLocales.firstOrNull() ?: Locale.ENGLISH
        return messageSource.getMessage(key, null, locale)
    }
}
