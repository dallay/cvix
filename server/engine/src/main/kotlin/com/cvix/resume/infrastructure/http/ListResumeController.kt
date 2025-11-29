package com.cvix.resume.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.resume.application.ResumeDocumentResponses
import com.cvix.resume.application.list.ListResumesQuery
import com.cvix.spring.boot.ApiController
import com.cvix.spring.boot.logging.LogMasker
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 *
 * @created 20/11/25
 */
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class ListResumeController(
    mediator: Mediator,
) : ApiController(mediator) {
    @Operation(
        summary = "List all resumes for a user in a workspace",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Resumes retrieved successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        ApiResponse(responseCode = "401", description = "Unauthorized"),
    )
    @GetMapping("/resume")
    suspend fun listResumes(
        @Parameter(description = "Workspace ID") @RequestParam workspaceId: UUID,
        @Parameter(description = "Maximum number of results (1-100)")
        @RequestParam(defaultValue = "50")
        @Min(1)
        @Max(100)
        limit: Int,
        @Parameter(description = "Cursor for pagination") @RequestParam(required = false) cursor: UUID?,
    ): ResponseEntity<ResumeDocumentResponses> {
        val userId = userIdFromToken()

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
