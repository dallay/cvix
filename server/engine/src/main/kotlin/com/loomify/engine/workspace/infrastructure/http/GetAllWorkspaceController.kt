package com.loomify.engine.workspace.infrastructure.http

import com.loomify.common.domain.bus.Mediator
import com.loomify.common.domain.bus.query.QueryResponse
import com.loomify.common.domain.bus.query.Response
import com.loomify.engine.workspace.application.find.member.AllWorkspaceByMemberQuery
import com.loomify.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * This controller handles the HTTP requests related to getting all workspaces.
 *
 * @property mediator The mediator used to handle queries.
 */
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
    @GetMapping("workspace")
    @ResponseBody
    suspend fun findAll(): Response {
        val authentication = authentication()
        val jwt = authentication?.principal as? Jwt
            ?: return QueryResponse("Authentication required")
        val userId = jwt.claims["sub"] as? String
        log.debug("Get All workspaces for user: {}", userId)
        if (userId.isNullOrBlank()) {
            return QueryResponse("Invalid authentication")
        }
        val response = ask(
            AllWorkspaceByMemberQuery(userId),
        )
        return response
    }

    companion object {
        private val log = LoggerFactory.getLogger(GetAllWorkspaceController::class.java)
    }
}
