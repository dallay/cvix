package com.cvix.workspace.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.common.domain.bus.query.Response
import com.cvix.spring.boot.ApiController
import com.cvix.workspace.application.find.FindWorkspaceQuery
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
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * This class is a REST controller that handles HTTP requests related to finding a workspace.
 * It extends the ApiController class and uses the Mediator pattern for handling queries.
 */
@Tag(
    name = "Workspace",
    description = "Workspace management endpoints",
)
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
    @Operation(
        summary = "Find a workspace by ID",
        description = "Retrieves the details of a specific workspace by its unique UUID. " +
            "Includes name, description, owner information, and current status.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Workspace found successfully",
                content = [
                    Content(
                        mediaType = "application/vnd.api.v1+json",
                        schema = Schema(implementation = Response::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Missing or invalid authentication token",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - User does not have access to this workspace",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Workspace not found",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error during workspace lookup",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
        ],
    )
    @GetMapping("/workspace/{id}")
    suspend fun find(
        @Parameter(
            name = "id",
            description = "The unique UUID of the workspace to find",
            required = true,
            `in` = ParameterIn.PATH,
            schema = Schema(type = "string", format = "uuid"),
            example = "550e8400-e29b-41d4-a716-446655440000",
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
