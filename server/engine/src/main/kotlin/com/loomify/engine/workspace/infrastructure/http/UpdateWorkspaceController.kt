package com.loomify.engine.workspace.infrastructure.http

import com.loomify.common.domain.bus.Mediator
import com.loomify.common.domain.presentation.SimpleMessageResponse
import com.loomify.engine.workspace.application.update.UpdateWorkspaceCommand
import com.loomify.engine.workspace.infrastructure.http.request.UpdateWorkspaceRequest
import com.loomify.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * This class is a REST controller for updating workspaces.
 * It extends the ApiController class and uses the Mediator pattern for handling requests.
 *
 * @property mediator The mediator used for handling requests.
 */
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
    @Operation(summary = "Update a workspace with the given data")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Updated workspace"),
        ApiResponse(responseCode = "400", description = "Bad request error (validation error)"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
    )
    @PutMapping("/workspace/{id}/update")
    suspend fun update(
        @Parameter(
            description = "ID of the workspace to be found",
            required = true,
            schema = Schema(type = "uuid", format = "uuid"),
        )
        @PathVariable
        id: UUID,
        @Validated @RequestBody request: UpdateWorkspaceRequest,
    ): ResponseEntity<SimpleMessageResponse> {
        log.debug("Updating workspace with ID: {}", id)
        dispatch(
            UpdateWorkspaceCommand(id, request.name, request.description?.takeIf { it.isNotBlank() },
            ),
        )
        return ResponseEntity.ok(SimpleMessageResponse("Workspace updated successfully."))
    }

    companion object {
        private val log = LoggerFactory.getLogger(UpdateWorkspaceController::class.java)
    }
}
