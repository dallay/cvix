package com.cvix.form.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.form.application.delete.DeleteSubscriberFormCommand
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
import java.util.*
import org.springframework.context.MessageSource
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for deleting subscription forms.
 *
 * This endpoint handles the removal of a subscription form configuration.
 */
@Validated
@RestController
@RequestMapping(value = ["/api/v1/subscription-forms"])
@Tag(name = "Subscription Form")
class DeleteSubscriptionFormController(
    mediator: Mediator,
    private val messageSource: MessageSource,
) : ApiController(mediator) {

    @Operation(
        summary = "Delete a subscription form",
        description = "Deletes an existing subscription form configuration.",
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
                description = "Form deleted successfully",
                content = [Content(schema = Schema(implementation = MessageResponse::class))],
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

    private fun getLocalizedMessage(key: String, request: ServerHttpRequest): String {
        val locale = request.headers.acceptLanguageAsLocales.firstOrNull() ?: Locale.ENGLISH
        return messageSource.getMessage(key, null, locale)
    }
}
