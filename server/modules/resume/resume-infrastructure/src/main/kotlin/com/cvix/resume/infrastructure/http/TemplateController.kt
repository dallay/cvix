package com.cvix.resume.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.resume.application.TemplateMetadataResponses
import com.cvix.resume.application.template.ListTemplatesQuery
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
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.slf4j.LoggerFactory
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for template management endpoints.
 */
@Tag(
    name = "Resume",
    description = "Resume/CV document management endpoints",
)
@RestController
@RequestMapping(value = ["/api/templates"], produces = ["application/vnd.api.v1+json"])
@Validated
class TemplateController(
    mediator: Mediator,
) : ApiController(mediator) {

    @Operation(
        summary = "List available resume templates",
        description = "Retrieves a list of metadata for available resume templates. " +
            "Requires X-Workspace-Id header for context.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "X-Workspace-Id",
                description = "The ID of the workspace context",
                required = true,
                `in` = ParameterIn.HEADER,
                schema = Schema(type = "string", format = "uuid"),
                example = "123e4567-e89b-12d3-a456-426614174000",
            ),
        ],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "List of templates retrieved successfully",
                content = [Content(schema = Schema(implementation = TemplateMetadataResponses::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters or missing X-Workspace-Id header",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid token",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - insufficient permissions",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
        ],
    )
    @GetMapping
    suspend fun listTemplates(
        @Parameter(
            description = "Maximum number of templates to return (1-50)",
            example = "50",
            `in` = ParameterIn.QUERY,
        )
        @RequestParam(name = "limit", required = false)
        @Min(1) @Max(50) limit: Int?,
    ): ResponseEntity<TemplateMetadataResponses> {
        val workspaceId = workspaceIdFromContext()
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
