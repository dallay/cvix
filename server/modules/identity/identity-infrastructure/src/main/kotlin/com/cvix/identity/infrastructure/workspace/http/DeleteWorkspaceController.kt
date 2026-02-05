package com.cvix.identity.infrastructure.workspace.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.identity.application.workspace.delete.DeleteWorkspaceCommand
import com.cvix.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * This class is a REST controller that handles HTTP requests related to workspace deletion.
 * It extends the ApiController class and uses the Mediator pattern for handling commands.
 */
@Tag(
    name = "Workspace",
    description = "Workspace management endpoints",
)
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class DeleteWorkspaceController(
    mediator: Mediator,
) : ApiController(mediator) {

    /**
     * This function handles the DELETE HTTP request for deleting a workspace.
     * It uses the path variable 'id' to identify the workspace to be deleted.
     * The function is a suspend function, meaning it is designed to be used with Kotlin coroutines.
     * It dispatches a [DeleteWorkspaceCommand] with the provided id.
     *
     * @param id The id of the workspace to be deleted.
     * @return The result of the [DeleteWorkspaceCommand] dispatch.
     */
    @Operation(
        summary = "Delete a workspace",
        description = "Permanently deletes a workspace and all associated resources. This operation cannot be undone.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Workspace deleted successfully",
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Missing or invalid authentication token",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - User does not have permission to delete this workspace",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Workspace not found",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error during workspace deletion",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
        ],
    )
    @DeleteMapping("/workspace/{id}")
    @ResponseStatus(HttpStatus.OK)
    suspend fun delete(
        @Parameter(
            name = "id",
            description = "The unique UUID of the workspace to delete",
            required = true,
            `in` = ParameterIn.PATH,
            schema = Schema(type = "string", format = "uuid"),
            example = "550e8400-e29b-41d4-a716-446655440000",
        )
        @PathVariable
        id: UUID,
    ) {
        log.debug("Deleting workspace with id: {}", id)
        dispatch(DeleteWorkspaceCommand(id))
    }

    companion object {
        private val log = LoggerFactory.getLogger(DeleteWorkspaceController::class.java)
    }
}
