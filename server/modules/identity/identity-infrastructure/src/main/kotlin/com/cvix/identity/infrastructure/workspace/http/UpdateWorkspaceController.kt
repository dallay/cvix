package com.cvix.identity.infrastructure.workspace.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.common.domain.presentation.SimpleMessageResponse
import com.cvix.identity.application.workspace.update.UpdateWorkspaceCommand
import com.cvix.identity.infrastructure.workspace.http.request.UpdateWorkspaceRequest
import com.cvix.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.parameters.RequestBody as OpenApiRequestBody

private const val UPDATE_WORKSPACE_EXAMPLE = """
    {
        "name": "Strategic Projects",
        "description": "High priority strategic initiatives and roadmap"
    }
"""

/**
 * This class is a REST controller for updating workspaces.
 * It extends the ApiController class and uses the Mediator pattern for handling requests.
 *
 * @property mediator The mediator used for handling requests.
 */
@Tag(
    name = "Workspace",
    description = "Workspace management endpoints",
)
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class UpdateWorkspaceController(
    private val mediator: Mediator,
) : ApiController(mediator) {

    /**
     * This method handles the PUT request for updating a workspace.
     * It validates the request body and dispatches an [UpdateWorkspaceCommand].
     *
     * @param id The ID of the workspace to update.
     * @param request The request body containing the new workspace data.
     * @return A ResponseEntity indicating the result of the operation.
     */
    @Operation(
        summary = "Update an existing workspace",
        description = "Modifies the name and description of an existing workspace. " +
            "Requires administrative permissions on the workspace.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Workspace updated successfully",
                content = [Content(schema = Schema(implementation = SimpleMessageResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid workspace data or missing required fields",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Missing or invalid authentication token",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - User does not have permission to update this workspace",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Workspace not found",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error during workspace update",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
        ],
    )
    @PutMapping("/workspace/{id}/update")
    suspend fun update(
        @Parameter(
            name = "id",
            description = "The unique UUID of the workspace to update",
            required = true,
            `in` = ParameterIn.PATH,
            schema = Schema(type = "string", format = "uuid"),
            example = "550e8400-e29b-41d4-a716-446655440000",
        )
        @PathVariable
        id: UUID,
        @OpenApiRequestBody(
            description = "Updated workspace details",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UpdateWorkspaceRequest::class),
                    examples = [
                        ExampleObject(
                            name = "Update name and description",
                            value = UPDATE_WORKSPACE_EXAMPLE,
                        ),
                    ],
                ),
            ],
        )
        @Validated @RequestBody request: UpdateWorkspaceRequest,
    ): ResponseEntity<SimpleMessageResponse> {
        log.debug("Updating workspace with ID: {}", id)
        dispatch(
            UpdateWorkspaceCommand(
                id, request.name, request.description?.takeIf { it.isNotBlank() },
            ),
        )
        return ResponseEntity.ok(SimpleMessageResponse("Workspace updated successfully."))
    }

    companion object {
        private val log = LoggerFactory.getLogger(UpdateWorkspaceController::class.java)
    }
}
