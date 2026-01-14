package com.cvix.workspace.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.common.domain.bus.query.Response
import com.cvix.spring.boot.ApiController
import com.cvix.workspace.application.find.member.AllWorkspaceByMemberQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * This controller handles the HTTP requests related to getting all workspaces.
 *
 * @property mediator The mediator used to handle queries.
 */
@Tag(
    name = "Workspace",
    description = "Workspace management endpoints",
)
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class GetAllWorkspaceController(
    mediator: Mediator
) : ApiController(mediator) {

    /**
     * This function handles the GET request to get all workspaces.
     *
     * It logs the action and then uses the mediator to ask for all workspaces.
     * It then returns the response from the mediator.
     *
     * @return A Response object containing the result of the query.
     */
    @Operation(summary = "Get all workspaces")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Success"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
    )
    @GetMapping("/workspace")
    @ResponseBody
    suspend fun findAll(): Response {
        val userId = userIdFromToken()

        log.debug("Get All workspaces for user: {}", userId)
        val response = ask(
            AllWorkspaceByMemberQuery(userId),
        )
        return response
    }

    companion object {
        private val log = LoggerFactory.getLogger(GetAllWorkspaceController::class.java)
    }
}
