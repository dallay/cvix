package com.cvix.resume.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.resume.application.ResumeDocumentResponses
import com.cvix.resume.application.list.ListResumesQuery
import com.cvix.spring.boot.ApiController
import com.cvix.spring.boot.logging.LogMasker
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
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for listing resumes.
 *
 * Note: The workspace ID is obtained from the X-Workspace-Id header,
 * not from query parameters. This ensures a single source of truth
 * for workspace context and enables Row-Level Security (RLS).
 *
 * @created 20/11/25
 */
@Tag(
    name = "Resume",
    description = "Resume/CV document management endpoints",
)
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class ListResumeController(
    mediator: Mediator,
) : ApiController(mediator) {
    @Operation(
        summary = "List all resumes for a user in a workspace",
        description = "Retrieves a paginated list of resumes within the specified workspace. " +
            "Requires the X-Workspace-Id header to provide workspace context.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "X-Workspace-Id",
                description = "The ID of the workspace to list resumes from",
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
                description = "Resumes retrieved successfully",
                content = [Content(schema = Schema(implementation = ResumeDocumentResponses::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters or missing X-Workspace-Id header",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Missing or invalid authentication token",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - User does not have access to the specified workspace",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
        ],
    )
    @GetMapping("/resume")
    suspend fun listResumes(
        @Parameter(
            description = "Maximum number of results to return",
            example = "50",
            `in` = ParameterIn.QUERY,
        )
        @RequestParam(defaultValue = "50")
        @Min(1)
        @Max(100)
        limit: Int,
        @Parameter(
            description = "Cursor for pagination (UUID of the last item in previous page)",
            `in` = ParameterIn.QUERY,
        )
        @RequestParam(required = false)
        cursor: UUID?,
    ): ResponseEntity<ResumeDocumentResponses> {
        val userId = userIdFromToken()
        val workspaceId = workspaceIdFromContext()

        val maskedUserId = LogMasker.mask(userId)
        log.debug(
            "[ListResumes] userId(masked)={}, workspaceId={}, limit={}, cursor={}",
            maskedUserId,
            workspaceId,
            limit,
            cursor,
        )

        val query = ListResumesQuery(
            userId = userId,
            workspaceId = workspaceId,
            limit = limit,
            cursor = cursor,
        )

        val documents = ask(query)

        log.debug("[ListResumes] Returned {} resumes", documents.data.size)

        return ResponseEntity.ok(documents)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ListResumeController::class.java)
    }
}
