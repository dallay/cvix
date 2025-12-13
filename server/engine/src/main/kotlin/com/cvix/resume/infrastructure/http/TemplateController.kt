package com.cvix.resume.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.resume.application.TemplateMetadataResponses
import com.cvix.resume.application.template.ListTemplatesQuery
import com.cvix.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for template management endpoints.
 */
@RestController
@RequestMapping(value = ["/api/templates"], produces = ["application/vnd.api.v1+json"])
@Validated
class TemplateController(
    mediator: Mediator,
) : ApiController(mediator) {

    @Operation(
        summary = "List available resume templates",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "List of templates retrieved successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
        ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
    )
    @GetMapping
    suspend fun listTemplates(
        @RequestParam(name = "limit", required = false)
        @Min(1) @Max(50) limit: Int?,
        @RequestParam(name = "workspaceId", required = true)
        workspaceId: java.util.UUID
    ): ResponseEntity<TemplateMetadataResponses> {
        log.debug("Fetching templates with limit={} for workspace={}", limit, workspaceId)
        val effectiveLimit = limit ?: DEFAULT_TEMPLATE_LIMIT
        val userId = userIdFromToken()
        val query = ListTemplatesQuery(userId, workspaceId, effectiveLimit)
        val templates = ask(query)
        return ResponseEntity.ok().body(templates)
    }

    companion object {
        private val log = LoggerFactory.getLogger(TemplateController::class.java)
        private const val DEFAULT_TEMPLATE_LIMIT = 50
    }
}
