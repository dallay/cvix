package com.cvix.config.infrastructure.http

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.ProblemDetail
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for providing application profile information.
 * This endpoint is used by the frontend to determine active profiles
 * and configure the ribbon display.
 */
@Tag(
    name = "System",
    description = "System configuration and profile information endpoints",
)
@RestController
@RequestMapping("/api", produces = ["application/vnd.api.v1+json"])
class ProfileInfoController(private val environment: Environment) {

    /**
     * Gets the active profiles and configuration information.
     *
     * @return Map containing active profiles and display settings in the format expected by the frontend
     */
    @Operation(
        summary = "Get application profile information",
        description = "Retrieves information about currently active Spring profiles and environment-specific configuration. " +
            "This information is typically used by the frontend to adjust the UI based on the environment " +
            "(e.g., displaying a development ribbon, enabling debug features, or changing API endpoints)."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Profile information retrieved successfully",
                content = [
                    Content(
                        mediaType = "application/vnd.api.v1+json",
                        schema = Schema(type = "object"),
                        examples = [
                            ExampleObject(
                                name = "Development Environment",
                                summary = "Response when running in 'dev' profile",
                                value = """
                                {
                                    "activeProfiles": ["dev", "tls"],
                                    "display-ribbon-on-profiles": "dev"
                                }
                                """
                            ),
                            ExampleObject(
                                name = "Production Environment",
                                summary = "Response when running in production profile",
                                value = """
                                {
                                    "activeProfiles": ["prod"],
                                    "display-ribbon-on-profiles": "none"
                                }
                                """
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error - Failed to retrieve profile information",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))]
            )
        ]
    )
    @GetMapping("/profile-info")
    fun getProfileInfo(): Map<String, Any?> {
        val activeProfiles = environment.activeProfiles.toList()
        val displayRibbonOnProfiles = environment.getProperty("application.display-ribbon-on-profiles")

        return mapOf(
            "activeProfiles" to activeProfiles,
            "display-ribbon-on-profiles" to displayRibbonOnProfiles,
        )
    }
}
