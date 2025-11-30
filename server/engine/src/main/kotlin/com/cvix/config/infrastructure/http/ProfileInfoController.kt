package com.cvix.config.infrastructure.http

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for providing application profile information.
 * This endpoint is used by the frontend to determine active profiles
 * and configure the ribbon display.
 */
@RestController
@RequestMapping("/api", produces = ["application/vnd.api.v1+json"])
class ProfileInfoController(private val environment: Environment) {

    /**
     * Gets the active profiles and configuration information.
     *
     * @return Map containing active profiles and display settings in the format expected by the frontend
     */
    @Operation(summary = "Get application profile information")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Profile information retrieved successfully"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
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
