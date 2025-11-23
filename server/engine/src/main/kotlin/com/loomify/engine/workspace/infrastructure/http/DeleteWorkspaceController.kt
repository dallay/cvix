package com.loomify.engine.workspace.infrastructure.http

import com.loomify.common.domain.bus.Mediator
import com.loomify.engine.workspace.application.delete.DeleteWorkspaceCommand
import com.loomify.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * This class is a REST controller that handles HTTP requests related to workspace deletion.
 * It extends the ApiController class and uses the Mediator pattern for handling commands.
 */
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
    @Operation(summary = "Delete a workspace")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Workspace deleted successfully"),
        ApiResponse(responseCode = "404", description = "Workspace not found"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
    )
    @DeleteMapping("/workspace/{id}")
    @ResponseStatus(HttpStatus.OK)
    suspend fun delete(
        @Parameter(
            description = "ID of the workspace to be found",
            required = true,
            schema = Schema(type = "uuid", format = "uuid"),
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
