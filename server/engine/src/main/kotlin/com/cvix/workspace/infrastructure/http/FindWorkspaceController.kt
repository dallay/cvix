package com.cvix.workspace.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.common.domain.bus.query.Response
import com.cvix.spring.boot.ApiController
import com.cvix.workspace.application.find.FindWorkspaceQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * This class is a REST controller that handles HTTP requests related to finding a workspace.
 * It extends the ApiController class and uses the Mediator pattern for handling queries.
 */
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class FindWorkspaceController(
    mediator: Mediator,
) : ApiController(mediator) {

    /**
     * This function handles the GET HTTP request for finding a workspace.
     * It uses the path variable 'id' to identify the workspace to be found.
     * The function is a suspend function, meaning it is designed to be used with Kotlin coroutines.
     * It dispatches a FindWorkspaceQuery with the provided id.
     * The function returns the response from the query.
     * @param id The id of the workspace to be found.
     * @return The result of the FindWorkspaceQuery dispatch.
     */
    @Operation(summary = "Find a workspace by ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Found workspace"),
        ApiResponse(responseCode = "404", description = "Workspace not found"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
    )
    @GetMapping("/workspace/{id}")
    suspend fun find(
        @Parameter(
            description = "ID of the workspace to be found",
            required = true,
            schema = Schema(type = "string", format = "uuid"),
        )
        @PathVariable
        id: UUID,
    ): Response {
        log.debug("Finding workspace")
        val query = FindWorkspaceQuery(id)
        val response = ask(query)
        return response
    }

    companion object {
        private val log = LoggerFactory.getLogger(FindWorkspaceController::class.java)
    }
}
